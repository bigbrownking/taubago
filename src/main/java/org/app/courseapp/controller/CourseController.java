package org.app.courseapp.controller;

import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.response.CourseDto;
import org.app.courseapp.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/my")
    public ResponseEntity<List<CourseDto>> getMyEnrolledCourses() {
        return ResponseEntity.ok(courseService.getMyEnrolledCourses());
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDto> getCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.getCourseById(courseId));
    }

    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<Void> enrollInCourse(@PathVariable Long courseId) {
        courseService.enrollInCourse(courseId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{courseId}/enroll")
    public ResponseEntity<Void> unenrollFromCourse(@PathVariable Long courseId) {
        courseService.unenrollFromCourse(courseId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/courses/my/active")
    public ResponseEntity<List<CourseDto>> getMyActiveCourses() {
        return ResponseEntity.ok(courseService.getMyActiveCourses());
    }

    @GetMapping("/courses/my/completed")
    public ResponseEntity<List<CourseDto>> getMyCompletedCourses() {
        return ResponseEntity.ok(courseService.getMyCompletedCourses());
    }

    @PostMapping("/courses/{courseId}/complete")
    public ResponseEntity<Void> completeCourse(@PathVariable Long courseId) {
        courseService.completeCourse(courseId);
        return ResponseEntity.ok().build();
    }
}