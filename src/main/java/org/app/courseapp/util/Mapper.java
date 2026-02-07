package org.app.courseapp.util;

import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.CourseDto;
import org.app.courseapp.dto.LessonDto;
import org.app.courseapp.dto.VideoDto;
import org.app.courseapp.model.Course;
import org.app.courseapp.model.Lesson;
import org.app.courseapp.model.Video;
import org.app.courseapp.model.VideoProgress;
import org.app.courseapp.repository.CourseEnrollmentRepository;
import org.app.courseapp.repository.VideoProgressRepository;
import org.app.courseapp.service.impl.MinioService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Mapper {

    private final MinioService minioService;
    private final VideoProgressRepository videoProgressRepository;
    private final CourseEnrollmentRepository enrollmentRepository;

    public CourseDto convertCourseToDto(Course course, Long userId) {
        boolean isEnrolled = userId != null &&
                enrollmentRepository.existsByUserIdAndCourseId(userId, course.getId());

        int enrolledCount = enrollmentRepository.findByCourseId(course.getId()).size();

        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .month(course.getMonth())
                .lessons(course.getLessons())
                .monthDisplayName(course.getMonth() != null ? course.getMonth().getDisplayName() : null)
                .durationDays(course.getDurationDays())
                .createdByName(course.getCreatedBy() != null ?
                        course.getCreatedBy().getName() + " " + course.getCreatedBy().getSurname() : null)
                .createdAt(course.getCreatedAt())
                .isEnrolled(isEnrolled)
                .enrolledCount(enrolledCount)
                .build();
    }
    public LessonDto convertLessonToDto(Lesson lesson, Long userId) {
        LessonDto dto = LessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .dayNumber(lesson.getDayNumber())
                .build();

        List<VideoDto> videoDtos = lesson.getVideos().stream()
                .map(video -> convertVideoToDto(video, userId))
                .toList();
        dto.setVideos(videoDtos);

        return dto;
    }

    public VideoDto convertVideoToDto(Video video, Long userId) {
        String videoUrl = minioService.getPresignedUrl(video.getObjectKey(), 2);

        VideoProgress progress = videoProgressRepository
                .findByUserIdAndVideoId(userId, video.getId())
                .orElse(null);

        return VideoDto.builder()
                .id(video.getId())
                .title(video.getTitle())
                .type(video.getType())
                .videoUrl(videoUrl)
                .durationSeconds(video.getDurationSeconds())
                .fileSizeBytes(video.getFileSizeBytes())
                .isCompleted(progress != null && progress.getIsCompleted())
                .watchedSeconds(progress != null ? progress.getWatchedSeconds() : 0L)
                .build();
    }
}