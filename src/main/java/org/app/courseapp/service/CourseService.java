package org.app.courseapp.service;

import org.app.courseapp.dto.response.CourseDto;
import org.app.courseapp.dto.request.CreateCourseRequest;

import java.util.List;

public interface CourseService {
    List<CourseDto> getAllCourses();
    List<CourseDto> getMyEnrolledCourses();
    CourseDto getCourseById(Long courseId);
    CourseDto createCourse(CreateCourseRequest request);
    void enrollInCourse(Long courseId);
    void unenrollFromCourse(Long courseId);
    boolean isEnrolled(Long courseId);
}