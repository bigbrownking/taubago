package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.config.minio.MinioProperties;
import org.app.courseapp.dto.request.CreateLessonReportRequest;
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
                String objectKey = String.format(
                        "courses/%d/lessons/%d/homework/user_%d_%d.%s",
                        lesson.getCourse().getId(),
                        lesson.getId(),
                        currentUser.getId(),
                        System.currentTimeMillis(),
                        getFileExtension(file.getOriginalFilename())
                );

                minioService.uploadFile(
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
                        .bucketName(minioProperties.getBucket())
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

        return convertToDto(report);
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "mp4";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "mp4";
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

    @Override
    @Transactional(readOnly = true)
    public List<ParentLessonReportFullDto> getFullReportsByLesson(Long lessonId) {
        return reportRepository.findByLessonId(lessonId).stream()
                .map(report -> buildFullDto(report, lessonId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ParentLessonReportFullDto getFullReportByLessonAndParent(Long lessonId, Long parentId) {
        LessonReport report = reportRepository
                .findByLessonIdAndParentId(lessonId, parentId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        return buildFullDto(report, lessonId);
    }

    private ParentLessonReportFullDto buildFullDto(LessonReport report, Long lessonId) {
        // Домашние видео этого родителя по уроку
        List<VideoDto> homeworkVideos = videoRepository
                .findByLessonIdAndUploadedById(lessonId, report.getParent().getId())
                .stream()
                .filter(v -> v.getType() == VideoType.HOMEWORK)
                .map(v -> mapper.convertVideoToDto(v, report.getParent().getId()))
                .toList();

        String parentName = "";
        if (report.getParent() instanceof Parent parent) {
            parentName = parent.getName() + " " + parent.getSurname();
        }

        return ParentLessonReportFullDto.builder()
                .parentId(report.getParent().getId())
                .parentName(parentName)
                .parentEmail(report.getParent().getEmail())
                .lessonId(report.getLesson().getId())
                .lessonTitle(report.getLesson().getTitle())
                .dayNumber(report.getLesson().getDayNumber())
                .childReactionRating(report.getChildReactionRating())
                .comment(report.getComment())
                .reportCreatedAt(report.getCreatedAt())
                .reportUpdatedAt(report.getUpdatedAt())
                .homeworkVideos(homeworkVideos)
                .build();
    }
}
