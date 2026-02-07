package org.app.courseapp.service;

import org.app.courseapp.dto.VideoDto;
import org.app.courseapp.dto.response.UploadUrlResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface VideoService {
    List<VideoDto> getVideosByLesson(Long lessonId, Long userId);
    List<VideoDto> uploadLessonVideos(Long lessonId, List<MultipartFile> file, String title) throws IOException;
    UploadUrlResponse getHomeworkUploadUrl(Long lessonId);
    VideoDto confirmHomeworkUpload(Long lessonId, String objectKey, String title, Long fileSize);
    void updateProgress(Long videoId, Long watchedSeconds);
    void markAsCompleted(Long videoId);
    void deleteVideo(Long videoId);
}
