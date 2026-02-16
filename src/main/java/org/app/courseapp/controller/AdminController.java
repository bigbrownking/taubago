package org.app.courseapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.dto.response.CourseDto;
import org.app.courseapp.dto.response.VideoDto;
import org.app.courseapp.dto.request.CreateCourseRequest;
import org.app.courseapp.dto.response.userProfile.BaseUserProfileDto;
import org.app.courseapp.model.CourseMonth;
import org.app.courseapp.service.CourseService;
import org.app.courseapp.service.UserService;
import org.app.courseapp.service.VideoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final CourseService courseService;
    private final VideoService videoService;

    @GetMapping("/profile/{email}")
    @Operation(summary = "Get user profile by email", description = "Get any user's profile (admin only)")
    public ResponseEntity<BaseUserProfileDto> getUserProfile(@PathVariable String email) {
        log.info("Fetching profile for user: {}", email);
        return ResponseEntity.ok(userService.getUserProfile(email));
    }

    @GetMapping("/months")
    public ResponseEntity<List<CourseMonth>> getAvailableMonths() {
        return ResponseEntity.ok(Arrays.asList(CourseMonth.values()));
    }

    @PostMapping("/course")
    public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.ok(courseService.createCourse(request));
    }

    @PostMapping(
            value = "/lesson/{lessonId}/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<List<VideoDto>> uploadLessonVideos(
            @PathVariable Long lessonId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("title") String title
    ) throws IOException {
        return ResponseEntity.ok(videoService.uploadLessonVideos(lessonId, files, title));
    }
}