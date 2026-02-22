package org.app.courseapp.service;

import org.app.courseapp.dto.response.LessonReportDto;
import org.app.courseapp.dto.response.ParentLessonReportFullDto;
import org.app.courseapp.dto.response.PublicLessonReportDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LessonReportService {
    LessonReportDto createOrUpdateReport(
            Long lessonId,
            Integer childReactionRating,
            String comment,
            List<MultipartFile> videos
    ) throws IOException;
    LessonReportDto getMyReport(Long lessonId);
    List<LessonReportDto> getMyAllReports();
    List<PublicLessonReportDto> getOtherParentsReports(Long lessonId);

    List<ParentLessonReportFullDto> getFullReportsByLesson(Long lessonId);
    ParentLessonReportFullDto getFullReportByLessonAndParent(Long lessonId, Long parentId);
}
