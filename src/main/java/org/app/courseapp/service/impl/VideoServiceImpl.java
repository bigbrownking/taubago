package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.config.minio.MinioProperties;
import org.app.courseapp.dto.response.VideoDto;
import org.app.courseapp.dto.response.UploadUrlResponse;
import org.app.courseapp.model.*;
import org.app.courseapp.model.users.User;
import org.app.courseapp.repository.*;
import org.app.courseapp.service.UserService;
import org.app.courseapp.service.VideoService;
import org.app.courseapp.util.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;
    private final LessonRepository lessonRepository;
    private final VideoProgressRepository videoProgressRepository;
    private final MinioService minioService;
    private final MinioProperties minioProperties;
    private final UserService userService;
    private final Mapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<VideoDto> getVideosByLesson(Long lessonId, Long userId) {
        User currentUser = userService.getCurrentUser();
        List<Video> videos = videoRepository.findByLessonId(lessonId);
        return videos.stream()
                .map(video -> mapper.convertVideoToDto(video, currentUser.getId()))
                .toList();
    }
    @Override
    public UploadUrlResponse getHomeworkUploadUrl(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        User currentUser = userService.getCurrentUser();
        String objectKey = generateHomeworkObjectKey(lesson, currentUser.getId());
        String uploadUrl = minioService.getPresignedUploadUrl(objectKey, 15);

        return new UploadUrlResponse(uploadUrl, objectKey, "video/mp4");
    }
    @Override
    @Transactional
    public void updateProgress(Long videoId, Long watchedSeconds) {
        User currentUser = userService.getCurrentUser();
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        VideoProgress progress = videoProgressRepository
                .findByUserIdAndVideoId(currentUser.getId(), videoId)
                .orElse(new VideoProgress());

        progress.setUser(currentUser);
        progress.setVideo(video);
        progress.setWatchedSeconds(watchedSeconds);

        if (video.getDurationSeconds() != null &&
                watchedSeconds >= video.getDurationSeconds() * 0.9) {
            progress.setIsCompleted(true);
            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(LocalDateTime.now());
            }
        }

        videoProgressRepository.save(progress);
    }

    @Override
    @Transactional
    public void markAsCompleted(Long videoId) {
        User currentUser = userService.getCurrentUser();
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        VideoProgress progress = videoProgressRepository
                .findByUserIdAndVideoId(currentUser.getId(), videoId)
                .orElse(new VideoProgress());

        progress.setUser(currentUser);
        progress.setVideo(video);
        progress.setIsCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        if (video.getDurationSeconds() != null) {
            progress.setWatchedSeconds(video.getDurationSeconds());
        }

        videoProgressRepository.save(progress);
    }

    @Override
    @Transactional
    public void deleteVideo(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        User currentUser = userService.getCurrentUser();

        if (!video.getUploadedBy().getId().equals(currentUser.getId()) &&
                !currentUser.hasRole("ROLE_ADMIN")) {
            throw new RuntimeException("Access denied");
        }

        minioService.deleteFile(video.getObjectKey());
        videoRepository.delete(video);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoDto> getLessonVideosByCategory(Long lessonId, VideoCategory category) {
        User currentUser = userService.getCurrentUser();
        List<Video> videos = videoRepository.findByLessonIdAndTypeAndCategory(
                lessonId, VideoType.LESSON, category
        );
        return videos.stream()
                .map(video -> mapper.convertVideoToDto(video, currentUser.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoDto> getMyHomeworkVideos(Long lessonId) {
        User currentUser = userService.getCurrentUser();
        return videoRepository.findByLessonIdAndUploadedById(lessonId, currentUser.getId())
                .stream()
                .filter(v -> v.getType() == VideoType.HOMEWORK)
                .map(video -> mapper.convertVideoToDto(video, currentUser.getId()))
                .toList();
    }

    @Transactional
    public VideoDto uploadLessonVideo(
            Long lessonId,
            MultipartFile file,
            String title,
            VideoCategory category
    ) throws IOException {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        User currentUser = userService.getCurrentUser();

        if (!currentUser.hasRole("ROLE_ADMIN")) {
            throw new RuntimeException("Only admins can upload lesson videos");
        }

        if (category == null) {
            throw new RuntimeException("Category is required for lesson videos");
        }

        String objectKey = generateObjectKey(lesson, VideoType.LESSON, category, file.getOriginalFilename());

        minioService.uploadFile(
                objectKey,
                file.getInputStream(),
                file.getContentType(),
                file.getSize()
        );

        Video video = videoRepository.save(
                Video.builder()
                        .title(title)
                        .type(VideoType.LESSON)
                        .category(category)
                        .objectKey(objectKey)
                        .bucketName(minioProperties.getBucket())
                        .fileSizeBytes(file.getSize())
                        .contentType(file.getContentType())
                        .lesson(lesson)
                        .uploadedBy(currentUser)
                        .build()
        );

        log.info("Admin {} uploaded {} video for lesson {}",
                currentUser.getEmail(), category, lessonId);

        return mapper.convertVideoToDto(video, currentUser.getId());
    }

    @Override
    @Transactional
    public VideoDto confirmHomeworkUpload(Long lessonId, String objectKey, String title, Long fileSize) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        User currentUser = userService.getCurrentUser();

        Video video = Video.builder()
                .title(title)
                .type(VideoType.HOMEWORK)
                .category(null)
                .objectKey(objectKey)
                .bucketName(minioProperties.getBucket())
                .fileSizeBytes(fileSize)
                .contentType("video/mp4")
                .lesson(lesson)
                .uploadedBy(currentUser)
                .build();

        video = videoRepository.save(video);

        log.info("User {} uploaded homework for lesson {}", currentUser.getEmail(), lessonId);

        return mapper.convertVideoToDto(video, currentUser.getId());
    }

    private String generateObjectKey(Lesson lesson, VideoType type, VideoCategory category, String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String categoryPath = category != null ? category.name().toLowerCase() : type.name().toLowerCase();
        return String.format(
                "courses/%d/lessons/%d/%s/%d.%s",
                lesson.getCourse().getId(),
                lesson.getId(),
                categoryPath,
                System.currentTimeMillis(),
                extension
        );
    }

    private String generateHomeworkObjectKey(Lesson lesson, Long userId) {
        return String.format(
                "courses/%d/lessons/%d/homework/user_%d_%d.mp4",
                lesson.getCourse().getId(),
                lesson.getId(),
                userId,
                System.currentTimeMillis()
        );
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "mp4";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "mp4";
    }
}