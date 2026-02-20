package org.app.courseapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.dto.response.CourseDto;
import org.app.courseapp.dto.request.CreateCourseRequest;
import org.app.courseapp.dto.response.userProfile.BaseUserProfileDto;
import org.app.courseapp.service.CourseService;
import org.app.courseapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final CourseService courseService;

    @GetMapping("/profile/{email}")
    @Operation(summary = "Get user profile by email", description = "Get any user's profile (admin only)")
    public ResponseEntity<BaseUserProfileDto> getUserProfile(@PathVariable String email) {
        log.info("Fetching profile for user: {}", email);
        return ResponseEntity.ok(userService.getUserProfile(email));
    }

    @PostMapping("/course")
    public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.ok(courseService.createCourse(request));
    }
}