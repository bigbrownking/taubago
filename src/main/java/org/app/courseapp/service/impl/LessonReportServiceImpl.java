package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.config.minio.MinioBucket;
import org.app.courseapp.config.minio.MinioProperties;
import org.app.courseapp.dto.response.LessonReportDto;
import org.app.courseapp.dto.response.ParentLessonReportFullDto;
import org.app.courseapp.dto.response.PublicLessonReportDto;
import org.app.courseapp.dto.response.VideoDto;
import org.app.courseapp.model.Lesson;
import org.app.courseapp.model.LessonReport;
import org.app.courseapp.model.Video;
import org.app.courseapp.model.VideoType;
import org.app.courseapp.model.users.Parent;
import org.app.courseapp.model.users.User;
import org.app.courseapp.repository.LessonReportRepository;
import org.app.courseapp.repository.LessonRepository;
import org.app.courseapp.repository.UserRepository;
import org.app.courseapp.repository.VideoRepository;
import org.app.courseapp.service.LessonReportService;
import org.app.courseapp.service.UserService;
import org.app.courseapp.util.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonReportServiceImpl implements LessonReportService {

    private final UserRepository userRepository;
    private final LessonReportRepository reportRepository;
    private final LessonRepository lessonRepository;
    private final UserService userService;
    private final MinioService minioService;
    private final MinioProperties minioProperties;
    private final VideoRepository videoRepository;
    private final Mapper mapper;

    @Override
    @Transactional
    public LessonReportDto createOrUpdateReport(
            Long lessonId,
            Integer childReactionRating,
            String comment,
            List<MultipartFile> videos
    ) throws IOException {
        User currentUser = userService.getCurrentUser();

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        LessonReport report = reportRepository
                .findByLessonIdAndParentId(lessonId, currentUser.getId())
                .orElse(LessonReport.builder()
                        .lesson(lesson)
                        .parent(currentUser)
                        .build());

        report.setChildReactionRating(childReactionRating);
        report.setComment(comment);
        reportRepository.save(report);

        if (videos != null && !videos.isEmpty()) {
            for (MultipartFile file : videos) {
                String objectKey = minioService.generateHomeworkKey(
                        lesson.getCourse().getId(),
                        lesson.getId(),
                        currentUser.getId(),
                        file.getOriginalFilename()
                );

                minioService.uploadFile(
                        MinioBucket.VIDEO,
                        objectKey,
                        file.getInputStream(),
                        file.getContentType(),
                        file.getSize()
                );

                Video video = Video.builder()
                        .title("Домашнее задание - День " + lesson.getDayNumber())
                        .type(VideoType.HOMEWORK)
                        .category(null)
                        .objectKey(objectKey)
                        .bucketName(MinioBucket.VIDEO.name())
                        .fileSizeBytes(file.getSize())
                        .contentType(file.getContentType())
                        .lesson(lesson)
                        .uploadedBy(currentUser)
                        .build();

                videoRepository.save(video);
            }
        }

        log.info("Parent {} submitted report for lesson {} with {} videos",
                currentUser.getEmail(), lessonId,
                videos != null ? videos.size() : 0);

        return mapper.convertToLessonReportDto(report);
    }

    @Override
    @Transactional(readOnly = true)
    public LessonReportDto getMyReport(Long lessonId) {
        User currentUser = userService.getCurrentUser();
        LessonReport report = reportRepository
                .findByLessonIdAndParentId(lessonId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Report not found"));
        return mapper.convertToLessonReportDto(report);
    }
    @Override
    @Transactional(readOnly = true)
    public List<LessonReportDto> getMyAllReports() {
        User currentUser = userService.getCurrentUser();
        return reportRepository.findByParentId(currentUser.getId()).stream()
                .map(mapper::convertToLessonReportDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicLessonReportDto> getOtherParentsReports(Long lessonId) {
        User currentUser = userService.getCurrentUser();

        return reportRepository.findByLessonId(lessonId).stream()
                .filter(r -> !r.getParent().getId().equals(currentUser.getId()))
                .map(mapper::convertToPublicLessonReportDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentLessonReportFullDto> getFullReportsByLesson(Long lessonId) {
        return reportRepository.findByLessonId(lessonId).stream()
                .map(report -> mapper.buildFullDto(report, lessonId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ParentLessonReportFullDto getFullReportByLessonAndParent(Long lessonId, Long parentId) {
        LessonReport report = reportRepository
                .findByLessonIdAndParentId(lessonId, parentId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        return mapper.buildFullDto(report, lessonId);
    }


}
