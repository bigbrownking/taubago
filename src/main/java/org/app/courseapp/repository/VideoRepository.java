package org.app.courseapp.repository;

import org.app.courseapp.model.Video;
import org.app.courseapp.model.VideoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByLessonIdAndType(Long lessonId, VideoType type);
    List<Video> findByLessonId(Long lessonId);
}