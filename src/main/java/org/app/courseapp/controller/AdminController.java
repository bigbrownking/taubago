package org.app.courseapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.CourseDto;
import org.app.courseapp.dto.VideoDto;
import org.app.courseapp.dto.request.CreateCourseRequest;
import org.app.courseapp.model.CourseMonth;
import org.app.courseapp.service.CourseService;
import org.app.courseapp.service.VideoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final CourseService courseService;
    private final VideoService videoService;

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