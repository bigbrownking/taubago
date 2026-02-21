package org.app.courseapp.service;

import org.app.courseapp.dto.request.CreateLessonReportRequest;
import org.app.courseapp.dto.response.LessonReportDto;
import org.app.courseapp.dto.response.PublicLessonReportDto;

import java.util.List;

public interface LessonReportService {
    LessonReportDto createOrUpdateReport(Long lessonId, CreateLessonReportRequest request);
    LessonReportDto getMyReport(Long lessonId);
    List<LessonReportDto> getReportsByLesson(Long lessonId);
    List<LessonReportDto> getMyAllReports();
    List<PublicLessonReportDto> getOtherParentsReports(Long lessonId);
}
