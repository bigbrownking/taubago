package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.dto.response.CourseDto;
import org.app.courseapp.dto.request.CreateCourseRequest;
import org.app.courseapp.model.Course;
import org.app.courseapp.model.CourseEnrollment;
import org.app.courseapp.model.Lesson;
import org.app.courseapp.model.users.Parent;
import org.app.courseapp.model.users.User;
import org.app.courseapp.repository.CourseEnrollmentRepository;
import org.app.courseapp.repository.CourseRepository;
import org.app.courseapp.repository.LessonRepository;
import org.app.courseapp.service.CourseService;
import org.app.courseapp.service.UserService;
import org.app.courseapp.util.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserService userService;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final Mapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getAllCourses() {
        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
        }

        Long userId = currentUser != null ? currentUser.getId() : null;
        return courseRepository.findAll().stream()
                .map(course -> mapper.convertCourseToDto(course, userId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getMyEnrolledCourses() {
        User currentUser = userService.getCurrentUser();
        List<CourseEnrollment> enrollments = enrollmentRepository.findByUserId(currentUser.getId());
        return enrollments.stream()
                .map(enrollment -> mapper.convertCourseToDto(enrollment.getCourse(), currentUser.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDto getCourseById(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
        }

        Long userId = currentUser != null ? currentUser.getId() : null;
        return mapper.convertCourseToDto(course, userId);
    }

    @Override
    @Transactional
    public CourseDto createCourse(CreateCourseRequest request) {
        User currentUser = userService.getCurrentUser();

        if (!currentUser.hasRole("ROLE_ADMIN")) {
            throw new RuntimeException("Access denied: Only administrators can create courses");
        }

        // Создаем курс
        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setDurationDays(request.getDurationDays());
        course.setCreatedBy(currentUser);

        course = courseRepository.save(course);


        int durationDays = course.getDurationDays();
        List<Lesson> lessons = new ArrayList<>();

        for (int day = 1; day <= durationDays; day++) {
            Lesson lesson = new Lesson();
            lesson.setTitle("День " + day);
            lesson.setDescription("Урок дня " + day);
            lesson.setDayNumber(day);
            lesson.setCourse(course);

            course.addLesson(lesson);
            lessons.add(lesson);
        }

        courseRepository.save(course);

        log.info("Admin {} created course: {} for days {} with {} lessons",
                currentUser.getEmail(),
                course.getTitle(),
                course.getDurationDays(),
                lessons.size());

        return mapper.convertCourseToDto(course, currentUser.getId());
    }

    @Override
    @Transactional
    public void enrollInCourse(Long courseId) {
        User currentUser = userService.getCurrentUser();

        if (!(currentUser instanceof Parent)) {
            throw new RuntimeException("Only parents can enroll in courses");
        }

        // Проверяем что нет активного курса прямо сейчас
        boolean hasActiveCourse = enrollmentRepository
                .findByUserId(currentUser.getId())
                .stream()
                .anyMatch(e -> !Boolean.TRUE.equals(e.getCompleted()));

        if (hasActiveCourse) {
            throw new RuntimeException("You must complete your current course before enrolling in a new one");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Проверяем что предыдущий курс по порядку завершён
        if (course.getOrder() != null && course.getOrder() > 1) {
            Course previousCourse = courseRepository.findByOrder(course.getOrder() - 1)
                    .orElseThrow(() -> new RuntimeException("Previous course not found"));

            boolean previousCompleted = enrollmentRepository
                    .findByUserIdAndCourseId(currentUser.getId(), previousCourse.getId())
                    .map(e -> Boolean.TRUE.equals(e.getCompleted()))
                    .orElse(false);

            if (!previousCompleted) {
                throw new RuntimeException("You must complete the previous course first");
            }
        }

        if (enrollmentRepository.existsByUserIdAndCourseId(currentUser.getId(), courseId)) {
            throw new RuntimeException("Already enrolled in this course");
        }

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setUser(currentUser);
        enrollment.setCourse(course);
        enrollmentRepository.save(enrollment);

        log.info("User {} enrolled in course {}", currentUser.getEmail(), course.getTitle());
    }

    @Override
    @Transactional
    public void unenrollFromCourse(Long courseId) {
        User currentUser = userService.getCurrentUser();

        CourseEnrollment enrollment = enrollmentRepository
                .findByUserIdAndCourseId(currentUser.getId(), courseId)
                .orElseThrow(() -> new RuntimeException("Not enrolled in this course"));

        enrollmentRepository.delete(enrollment);
        log.info("User {} unenrolled from course {}", currentUser.getEmail(), courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEnrolled(Long courseId) {
        User currentUser = userService.getCurrentUser();
        return enrollmentRepository.existsByUserIdAndCourseId(currentUser.getId(), courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getMyActiveCourses() {
        User currentUser = userService.getCurrentUser();
        return enrollmentRepository.findByUserIdAndCompleted(currentUser.getId(), false).stream()
                .map(e -> mapper.convertCourseToDto(e.getCourse(), currentUser.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getMyCompletedCourses() {
        User currentUser = userService.getCurrentUser();
        return enrollmentRepository.findByUserIdAndCompleted(currentUser.getId(), true).stream()
                .map(e -> mapper.convertCourseToDto(e.getCourse(), currentUser.getId()))
                .toList();
    }

    @Override
    @Transactional
    public void completeCourse(Long courseId) {
        User currentUser = userService.getCurrentUser();

        if (!(currentUser instanceof Parent)) {
            throw new RuntimeException("Only parents can complete courses");
        }

        CourseEnrollment enrollment = enrollmentRepository
                .findByUserIdAndCourseId(currentUser.getId(), courseId)
                .orElseThrow(() -> new RuntimeException("Not enrolled in this course"));

        if (Boolean.TRUE.equals(enrollment.getCompleted())) {
            throw new RuntimeException("Course already completed");
        }

        enrollment.setCompleted(true);
        enrollment.setCompletedAt(LocalDateTime.now());
        enrollment.setProgressPercentage(100);
        enrollmentRepository.save(enrollment);

        log.info("User {} completed course {}", currentUser.getEmail(), courseId);
    }
}