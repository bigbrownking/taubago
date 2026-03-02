package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.config.minio.MinioProperties;
import org.app.courseapp.dto.response.VideoDto;
import org.app.courseapp.model.*;
import org.app.courseapp.model.users.Parent;
import org.app.courseapp.model.users.User;
import org.app.courseapp.repository.*;
import org.app.courseapp.service.UserService;
import org.app.courseapp.service.VideoService;
import org.app.courseapp.util.CompletionChecker;
import org.app.courseapp.util.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoServiceImpl implements VideoService {

    private final CourseEnrollmentRepository enrollmentRepository;
    private final VideoRepository videoRepository;
    private final LessonRepository lessonRepository;
    private final VideoProgressRepository videoProgressRepository;
    private final VideoCategoryRepository categoryRepository;
    private final ParentRepository parentRepository;
    private final MinioService minioService;
    private final MinioProperties minioProperties;
    private final UserService userService;
    private final Mapper mapper;

    private final CompletionChecker completionChecker;

    @Override
    @Transactional(readOnly = true)
    public List<VideoDto> getVideosByLesson(Long lessonId) {
        User currentUser = userService.getCurrentUser();
        List<Video> videos = videoRepository.findByLessonId(lessonId);

        // Фильтруем видео по доступу для родителей
        return videos.stream()
                .filter(video -> hasAccessToVideo(currentUser, video))
                .map(video -> mapper.convertVideoToDto(video, currentUser.getId()))
                .toList();
    }

    @Override
    @Transactional
    public void updateProgress(Long videoId, Long watchedSeconds) {
        User currentUser = userService.getCurrentUser();
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        // Проверка доступа
        if (!hasAccessToVideo(currentUser, video)) {
            throw new RuntimeException("Access denied: You don't have access to this video category");
        }

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

        // Проверка доступа
        if (!hasAccessToVideo(currentUser, video)) {
            throw new RuntimeException("Access denied: You don't have access to this video category");
        }

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
        updateCourseProgress(currentUser.getId(), video.getLesson().getCourse().getId());
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
    public List<VideoDto> getMyHomeworkVideos(Long lessonId) {
        User currentUser = userService.getCurrentUser();
        return videoRepository.findByLessonIdAndUploadedById(lessonId, currentUser.getId())
                .stream()
                .filter(v -> v.getType() == VideoType.HOMEWORK)
                .map(video -> mapper.convertVideoToDto(video, currentUser.getId()))
                .toList();
    }

    @Override
    @Transactional
    public List<VideoDto> uploadLessonVideo(
            Long lessonId,
            List<MultipartFile> files,
            String title,
            Long categoryId
    ) throws IOException {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        User currentUser = userService.getCurrentUser();

        if (!currentUser.hasRole("ROLE_ADMIN")) {
            throw new RuntimeException("Only admins can upload lesson videos");
        }

        VideoCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        List<Video> videos = new ArrayList<>();

        for (MultipartFile file : files) {
            String objectKey = minioService.generateLessonVideoKey(
                    lesson.getCourse().getId(),
                    lesson.getId(),
                    category.getName(),
                    file.getOriginalFilename()
            );

            minioService.uploadFile(objectKey, file.getInputStream(), file.getContentType(), file.getSize());

            Video video = Video.builder()
                    .title(title)
                    .type(VideoType.LESSON)
                    .category(category)
                    .objectKey(objectKey)
                    .bucketName(minioProperties.getBucket())
                    .fileSizeBytes(file.getSize())
                    .contentType(file.getContentType())
                    .lesson(lesson)
                    .uploadedBy(currentUser)
                    .build();

            videos.add(video);
        }

        videoRepository.saveAll(videos);

        return videos.stream()
                .map(v -> mapper.convertVideoToDto(v, currentUser.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoDto> getLessonVideosByCategory(Long lessonId, Long categoryId) {
        User currentUser = userService.getCurrentUser();
        VideoCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Проверка доступа родителя к этой категории
        if (currentUser instanceof Parent) {
            Parent parent = parentRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Parent not found"));

            if (!parent.hasAccessToCategory(category)) {
                throw new RuntimeException("Access denied: You don't have access to this video category");
            }
        }

        return videoRepository.findByLessonIdAndTypeAndCategory(lessonId, VideoType.LESSON, category)
                .stream()
                .map(video -> mapper.convertVideoToDto(video, currentUser.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VideoDto getVideoById(Long videoId) {
        User currentUser = userService.getCurrentUser();
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        // Проверка доступа
        if (!hasAccessToVideo(currentUser, video)) {
            throw new RuntimeException("Access denied: You don't have access to this video category");
        }

        return mapper.convertVideoToDto(video, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccessToVideo(Long videoId) {
        User currentUser = userService.getCurrentUser();
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        return hasAccessToVideo(currentUser, video);
    }

    /**
     * Проверка доступа пользователя к видео
     */
    private boolean hasAccessToVideo(User user, Video video) {
        // Админы и кураторы имеют доступ ко всему
        if (user.hasRole("ROLE_ADMIN") || user.hasRole("ROLE_CURATOR")) {
            return true;
        }

        // Если у видео нет категории - доступ для всех
        if (video.getCategory() == null) {
            return true;
        }

        // Для родителей проверяем доступ к категории
        if (user instanceof Parent) {
            Parent parent = parentRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Parent not found"));
            return parent.hasAccessToCategory(video.getCategory());
        }

        // Для других ролей - запрет по умолчанию
        return false;
    }

    private void updateCourseProgress(Long userId, Long courseId) {
        CourseEnrollment enrollment = enrollmentRepository
                .findByUserIdAndCourseId(userId, courseId)
                .orElse(null);

        if (enrollment != null) {
            long newProgress = calculateCourseProgress(courseId, userId);
            enrollment.setProgressPercentage(newProgress);

            if (newProgress >= 100 && !Boolean.TRUE.equals(enrollment.getCompleted())) {
                enrollment.setCompleted(true);
                enrollment.setCompletedAt(LocalDateTime.now());
                log.info("User {} completed course {}", userId, courseId);
            }

            enrollmentRepository.save(enrollment);
        }
    }
    private int calculateCourseProgress(Long courseId, Long userId) {
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByDayNumber(courseId);
        if (lessons.isEmpty()) {
            return 0;
        }

        long completedLessons = lessons.stream()
                .filter(lesson -> completionChecker.isLessonCompleted(lesson, userId))
                .count();

        return (int) ((completedLessons * 100.0) / lessons.size());
    }


}