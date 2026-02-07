package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.config.minio.MinioProperties;
import org.app.courseapp.dto.VideoDto;
import org.app.courseapp.dto.response.UploadUrlResponse;
import org.app.courseapp.model.*;
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

    @Transactional
    public List<VideoDto> uploadLessonVideos(
            Long lessonId,
            List<MultipartFile> files,
            String title
    ) throws IOException {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        User currentUser = userService.getCurrentUser();

        List<VideoDto> result = new ArrayList<>();

        for (MultipartFile file : files) {
            String objectKey = generateObjectKey(
                    lesson, VideoType.LESSON, file.getOriginalFilename()
            );

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
                            .objectKey(objectKey)
                            .bucketName(minioProperties.getBucket())
                            .fileSizeBytes(file.getSize())
                            .contentType(file.getContentType())
                            .lesson(lesson)
                            .uploadedBy(currentUser)
                            .build()
            );

            result.add(mapper.convertVideoToDto(video, currentUser.getId()));
        }

        return result;
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
    public VideoDto confirmHomeworkUpload(Long lessonId, String objectKey, String title, Long fileSize) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        User currentUser = userService.getCurrentUser();

        Video video = Video.builder()
                .title(title)
                .type(VideoType.HOMEWORK)
                .objectKey(objectKey)
                .bucketName(minioProperties.getBucket())
                .fileSizeBytes(fileSize)
                .contentType("video/mp4")
                .lesson(lesson)
                .uploadedBy(currentUser)
                .build();

        video = videoRepository.save(video);
        return mapper.convertVideoToDto(video, currentUser.getId());
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

    private String generateObjectKey(Lesson lesson, VideoType type, String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return String.format(
                "courses/%d/lessons/%d/%s/%d_%s",
                lesson.getCourse().getId(),
                lesson.getId(),
                type.name().toLowerCase(),
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