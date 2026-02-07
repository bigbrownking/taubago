package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.dto.CourseDto;
import org.app.courseapp.dto.request.CreateCourseRequest;
import org.app.courseapp.model.Course;
import org.app.courseapp.model.CourseEnrollment;
import org.app.courseapp.model.Lesson;
import org.app.courseapp.model.User;
import org.app.courseapp.repository.CourseEnrollmentRepository;
import org.app.courseapp.repository.CourseRepository;
import org.app.courseapp.repository.LessonRepository;
import org.app.courseapp.service.CourseService;
import org.app.courseapp.service.UserService;
import org.app.courseapp.util.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserService userService;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
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
            throw new RuntimeException("Access denied: Only admins can create courses");
        }

        // Создаем курс
        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setMonth(request.getMonth());
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
        }

        lessonRepository.saveAll(lessons);

        log.info("Admin {} created course: {} for month {} ({} days) with {} lessons",
                currentUser.getEmail(),
                course.getTitle(),
                course.getMonth().getDisplayName(),
                course.getDurationDays(),
                lessons.size());

        return mapper.convertCourseToDto(course, currentUser.getId());
    }

    @Override
    @Transactional
    public void enrollInCourse(Long courseId) {
        User currentUser = userService.getCurrentUser();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

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
}