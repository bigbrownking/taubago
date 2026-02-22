package org.app.courseapp.service;

import org.app.courseapp.dto.response.VideoDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface VideoService {
    List<VideoDto> getVideosByLesson(Long lessonId);
    void updateProgress(Long videoId, Long watchedSeconds);
    void markAsCompleted(Long videoId);
    void deleteVideo(Long videoId);
    List<VideoDto> getLessonVideosByCategory(Long lessonId, Long categoryId);
    List<VideoDto> getMyHomeworkVideos(Long lessonId);
    VideoDto uploadLessonVideo(Long lessonId, MultipartFile file, String title, Long categoryId) throws IOException;
}
