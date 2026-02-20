package org.app.courseapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.request.CreateLessonReportRequest;
import org.app.courseapp.dto.response.LessonDto;
import org.app.courseapp.dto.response.LessonReportDto;
import org.app.courseapp.dto.response.PublicLessonReportDto;
import org.app.courseapp.service.LessonReportService;
import org.app.courseapp.service.LessonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/{lessonId}/report")
    public ResponseEntity<LessonReportDto> submitReport(
            @PathVariable Long lessonId,
            @RequestBody @Valid CreateLessonReportRequest request
    ) {
        return ResponseEntity.ok(reportService.createOrUpdateReport(lessonId, request));
    }

    @GetMapping("/{lessonId}/reports")
    public ResponseEntity<List<LessonReportDto>> getReportsByLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(reportService.getReportsByLesson(lessonId));
    }
    @GetMapping("/{lessonId}/report/my")
    public ResponseEntity<LessonReportDto> getMyReport(@PathVariable Long lessonId) {
        return ResponseEntity.ok(reportService.getMyReport(lessonId));
    }
    @GetMapping("/reports/my")
    public ResponseEntity<List<LessonReportDto>> getMyAllReports() {
        return ResponseEntity.ok(reportService.getMyAllReports());
    }
    @GetMapping("/{lessonId}/reports/community")
    public ResponseEntity<List<PublicLessonReportDto>> getOtherParentsReports(
            @PathVariable Long lessonId
    ) {
        return ResponseEntity.ok(reportService.getOtherParentsReports(lessonId));
    }
}