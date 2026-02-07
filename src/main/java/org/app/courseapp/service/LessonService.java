package org.app.courseapp.service;

import org.app.courseapp.dto.LessonDto;

import java.util.List;

public interface LessonService {
    List<LessonDto> getLessonsByCourse(Long courseId);
    LessonDto getLessonById(Long lessonId);
}
