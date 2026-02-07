package org.app.courseapp.repository;

import org.app.courseapp.model.VideoProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoProgressRepository extends JpaRepository<VideoProgress, Long> {
    Optional<VideoProgress> findByUserIdAndVideoId(Long userId, Long videoId);
    List<VideoProgress> findByUserId(Long userId);
    List<VideoProgress> findByVideoId(Long videoId);
    List<VideoProgress> findByUserIdAndVideoLessonId(Long userId, Long lessonId);
}