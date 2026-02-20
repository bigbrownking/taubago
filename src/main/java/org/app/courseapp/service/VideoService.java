package org.app.courseapp.service;

import org.app.courseapp.dto.response.VideoDto;
import org.app.courseapp.dto.response.UploadUrlResponse;
import org.app.courseapp.model.VideoCategory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface VideoService {
    List<VideoDto> getVideosByLesson(Long lessonId, Long userId);
    UploadUrlResponse getHomeworkUploadUrl(Long lessonId);
    VideoDto confirmHomeworkUpload(Long lessonId, String objectKey, String title, Long fileSize);
    void updateProgress(Long videoId, Long watchedSeconds);
    void markAsCompleted(Long videoId);
    void deleteVideo(Long videoId);
    List<VideoDto> getLessonVideosByCategory(Long lessonId, VideoCategory category);
    List<VideoDto> getMyHomeworkVideos(Long lessonId);
    VideoDto uploadLessonVideo(Long lessonId, MultipartFile file, String title, VideoCategory category) throws IOException;
}
