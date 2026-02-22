package org.app.courseapp.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.request.CreateLessonReportRequest;
import org.app.courseapp.dto.response.LessonDto;
import org.app.courseapp.dto.response.LessonReportDto;
import org.app.courseapp.dto.response.ParentLessonReportFullDto;
import org.app.courseapp.dto.response.PublicLessonReportDto;
import org.app.courseapp.service.LessonReportService;
import org.app.courseapp.service.LessonService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    private final LessonReportService reportService;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LessonDto>> getLessonsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(lessonService.getLessonsByCourse(courseId));
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonDto> getLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(lessonService.getLessonById(lessonId));
    }
    @PostMapping(value = "/{lessonId}/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<LessonReportDto> submitReport(
            @PathVariable Long lessonId,
            @RequestParam("childReactionRating") @Min(1) @Max(5) Integer childReactionRating,
            @RequestParam(value = "comment", required = false) String comment,
            @RequestParam(value = "videos", required = false) List<MultipartFile> videos
    ) throws IOException {
        return ResponseEntity.ok(
                reportService.createOrUpdateReport(lessonId, childReactionRating, comment, videos)
        );
    }
    @GetMapping("/{lessonId}/report/my")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<LessonReportDto> getMyReport(@PathVariable Long lessonId) {
        return ResponseEntity.ok(reportService.getMyReport(lessonId));
    }
    @GetMapping("/reports/my")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<LessonReportDto>> getMyAllReports() {
        return ResponseEntity.ok(reportService.getMyAllReports());
    }
    @GetMapping("/{lessonId}/reports/community")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<PublicLessonReportDto>> getOtherParentsReports(
            @PathVariable Long lessonId
    ) {
        return ResponseEntity.ok(reportService.getOtherParentsReports(lessonId));
    }
    @GetMapping("/current")
    public ResponseEntity<LessonDto> getCurrentLesson() {
        return ResponseEntity.ok(lessonService.getCurrentLesson());
    }

    @GetMapping("/{lessonId}/reports/full")
    @PreAuthorize("hasAnyRole('CURATOR', 'ADMIN')")
    public ResponseEntity<List<ParentLessonReportFullDto>> getFullReportsByLesson(
            @PathVariable Long lessonId
    ) {
        return ResponseEntity.ok(reportService.getFullReportsByLesson(lessonId));
    }

    @GetMapping("/{lessonId}/reports/full/{parentId}")
    @PreAuthorize("hasAnyRole('CURATOR', 'ADMIN')")
    public ResponseEntity<ParentLessonReportFullDto> getFullReportByLessonAndParent(
            @PathVariable Long lessonId,
            @PathVariable Long parentId
    ) {
        return ResponseEntity.ok(reportService.getFullReportByLessonAndParent(lessonId, parentId));
    }
}