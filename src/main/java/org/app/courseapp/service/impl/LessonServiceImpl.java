package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.response.LessonDto;
import org.app.courseapp.model.CourseEnrollment;
import org.app.courseapp.model.Lesson;
import org.app.courseapp.model.users.User;
import org.app.courseapp.repository.CourseEnrollmentRepository;
import org.app.courseapp.repository.LessonRepository;
import org.app.courseapp.service.LessonService;
import org.app.courseapp.service.UserService;
import org.app.courseapp.util.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final UserService userService;
    private final Mapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<LessonDto> getLessonsByCourse(Long courseId) {
        User currentUser = userService.getCurrentUser();
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByDayNumber(courseId);
        return lessons.stream()
                .map(lesson -> mapper.convertLessonToDto(lesson, currentUser.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LessonDto getLessonById(Long lessonId) {
        User currentUser = userService.getCurrentUser();
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        return mapper.convertLessonToDto(lesson, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public LessonDto getCurrentLesson() {
        User currentUser = userService.getCurrentUser();

        CourseEnrollment activeEnrollment = courseEnrollmentRepository
                .findByUserId(currentUser.getId())
                .stream()
                .filter(e -> !Boolean.TRUE.equals(e.getCompleted()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active course found"));

        Long courseId = activeEnrollment.getCourse().getId();

        Lesson currentLesson = lessonRepository
                .findFirstIncompleteLessonByCourseAndUser(courseId, currentUser.getId())
                .orElse(
                        lessonRepository.findByCourseIdOrderByDayNumber(courseId)
                                .stream()
                                .reduce((first, second) -> second)
                                .orElseThrow(() -> new RuntimeException("No lessons found"))
                );

        return mapper.convertLessonToDto(currentLesson, currentUser.getId());
    }
}