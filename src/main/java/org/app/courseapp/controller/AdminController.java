package org.app.courseapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.dto.request.GrantVideoCategoryAccessRequest;
import org.app.courseapp.dto.response.CourseDto;
import org.app.courseapp.dto.request.CreateCourseRequest;
import org.app.courseapp.dto.response.ParentVideoAccessDto;
import org.app.courseapp.dto.response.RegistrationQuestionDto;
import org.app.courseapp.dto.response.VideoCategoryDto;
import org.app.courseapp.dto.response.userProfile.BaseUserProfileDto;
import org.app.courseapp.model.VideoCategory;
import org.app.courseapp.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final RegistrationQuestionService registrationQuestionService;
    private final VideoCategoryService videoCategoryService;
    private final ParentVideoAccessService parentVideoAccessService;

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

    @PostMapping("/questions")
    public ResponseEntity<RegistrationQuestionDto> createQuestion(
            @RequestParam String question,
            @RequestParam String topic,
            @RequestParam Integer orderNumber
    ) {
        return ResponseEntity.ok(registrationQuestionService.create(question, topic, orderNumber));
    }

    @PostMapping("/video-categories")
    public ResponseEntity<Void> addVideoCategories(@RequestBody List<String> categories) {
        videoCategoryService.create(categories);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/video-categories")
    @Operation(summary = "Get all video categories")
    public ResponseEntity<List<VideoCategory>> getAllVideoCategories() {
        return ResponseEntity.ok(videoCategoryService.getAll());
    }

    @PostMapping("/parents/{parentId}/video-access/grant")
    @Operation(summary = "Grant video category access to parent",
            description = "Add specific video categories to parent's allowed list")
    public ResponseEntity<Void> grantVideoAccess(
            @PathVariable Long parentId,
            @Valid @RequestBody GrantVideoCategoryAccessRequest request) {
        log.info("Admin granting video access to parent {}", parentId);
        parentVideoAccessService.grantAccess(request.getParentId(), request.getCategoryIds());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/parents/{parentId}/video-access/revoke")
    @Operation(summary = "Revoke video category access from parent",
            description = "Remove specific video categories from parent's allowed list")
    public ResponseEntity<Void> revokeVideoAccess(
            @PathVariable Long parentId,
            @Valid @RequestBody GrantVideoCategoryAccessRequest request) {
        log.info("Admin revoking video access from parent {}", parentId);
        parentVideoAccessService.revokeAccess(request.getParentId(), request.getCategoryIds());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/parents/{parentId}/video-access")
    @Operation(summary = "Set video category access for parent",
            description = "Replace all video category access with new list")
    public ResponseEntity<Void> setVideoAccess(
            @PathVariable Long parentId,
            @Valid @RequestBody GrantVideoCategoryAccessRequest request) {
        log.info("Admin setting video access for parent {}", parentId);
        parentVideoAccessService.setAccess(request.getParentId(), request.getCategoryIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/parents/{parentId}/video-access")
    @Operation(summary = "Get parent's video category access",
            description = "Get all allowed and available video categories for a parent")
    public ResponseEntity<ParentVideoAccessDto> getParentVideoAccess(@PathVariable Long parentId) {
        log.info("Admin fetching video access for parent {}", parentId);
        return ResponseEntity.ok(parentVideoAccessService.getParentAccess(parentId));
    }
}