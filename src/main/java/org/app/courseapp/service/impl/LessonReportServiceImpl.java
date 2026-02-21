package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.dto.request.CreateLessonReportRequest;
import org.app.courseapp.dto.response.LessonReportDto;
import org.app.courseapp.dto.response.PublicLessonReportDto;
import org.app.courseapp.model.Lesson;
import org.app.courseapp.model.LessonReport;
import org.app.courseapp.model.users.Parent;
import org.app.courseapp.model.users.User;
import org.app.courseapp.repository.LessonReportRepository;
import org.app.courseapp.repository.LessonRepository;
import org.app.courseapp.service.LessonReportService;
import org.app.courseapp.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonReportServiceImpl implements LessonReportService {

    private final LessonReportRepository reportRepository;
    private final LessonRepository lessonRepository;
    private final UserService userService;

    @Override
    @Transactional
    public LessonReportDto createOrUpdateReport(Long lessonId, CreateLessonReportRequest request) {
        User currentUser = userService.getCurrentUser();

        if (!(currentUser instanceof Parent)) {
            throw new RuntimeException("Only parents can submit reports");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        // Если отчёт уже есть — обновляем, иначе создаём
        LessonReport report = reportRepository
                .findByLessonIdAndParentId(lessonId, currentUser.getId())
                .orElse(LessonReport.builder()
                        .lesson(lesson)
                        .parent(currentUser)
                        .build());

        report.setChildReactionRating(request.getChildReactionRating());
        report.setComment(request.getComment());
        reportRepository.save(report);

        log.info("Parent {} submitted report for lesson {}", currentUser.getEmail(), lessonId);
        return convertToDto(report);
    }

    @Override
    @Transactional(readOnly = true)
    public LessonReportDto getMyReport(Long lessonId) {
        User currentUser = userService.getCurrentUser();
        LessonReport report = reportRepository
                .findByLessonIdAndParentId(lessonId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Report not found"));
        return convertToDto(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonReportDto> getReportsByLesson(Long lessonId) {
        User currentUser = userService.getCurrentUser();
        if (!currentUser.hasRole("ROLE_ADMIN") && !currentUser.hasRole("ROLE_SPECIALIST")) {
            throw new RuntimeException("Access denied");
        }
        return reportRepository.findByLessonId(lessonId).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonReportDto> getMyAllReports() {
        User currentUser = userService.getCurrentUser();
        return reportRepository.findByParentId(currentUser.getId()).stream()
                .map(this::convertToDto)
                .toList();
    }

    private LessonReportDto convertToDto(LessonReport report) {
        LessonReportDto dto = new LessonReportDto();
        dto.setId(report.getId());
        dto.setLessonId(report.getLesson().getId());
        dto.setLessonTitle(report.getLesson().getTitle());
        dto.setDayNumber(report.getLesson().getDayNumber());
        dto.setChildReactionRating(report.getChildReactionRating());
        dto.setComment(report.getComment());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setUpdatedAt(report.getUpdatedAt());

        // Инфо о родителе (для куратора)
        if (report.getParent() instanceof Parent parent) {
            dto.setParentName(parent.getName() + " " + parent.getSurname());
            dto.setParentEmail(parent.getEmail());
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicLessonReportDto> getOtherParentsReports(Long lessonId) {
        User currentUser = userService.getCurrentUser();

        return reportRepository.findByLessonId(lessonId).stream()
                .filter(r -> !r.getParent().getId().equals(currentUser.getId()))
                .map(report -> {
                    PublicLessonReportDto dto = new PublicLessonReportDto();
                    dto.setChildReactionRating(report.getChildReactionRating());
                    dto.setComment(report.getComment());
                    dto.setCreatedAt(report.getCreatedAt());

                    if (report.getParent() instanceof Parent parent) {
                        dto.setParentName(parent.getName());
                    }
                    return dto;
                })
                .toList();
    }
}
