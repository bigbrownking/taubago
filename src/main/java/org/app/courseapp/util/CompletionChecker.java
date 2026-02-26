package org.app.courseapp.util;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.app.courseapp.model.Lesson;
import org.app.courseapp.model.Video;
import org.app.courseapp.model.VideoProgress;
import org.app.courseapp.repository.VideoProgressRepository;
import org.simpleframework.xml.core.Commit;
import org.simpleframework.xml.core.Complete;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CompletionChecker {
    private final VideoProgressRepository videoProgressRepository;

    public boolean isLessonCompleted(Lesson lesson, Long userId) {
        List<Video> lessonVideos = lesson.getLessonVideos();
        if (lessonVideos.isEmpty()) {
            return false;
        }
        return lessonVideos.stream()
                .allMatch(video -> isVideoCompleted(video.getId(), userId));
    }

    public boolean isVideoCompleted(Long videoId, Long userId) {
        Optional<VideoProgress> progress = videoProgressRepository
                .findByUserIdAndVideoId(userId, videoId);
        return progress.isPresent() && Boolean.TRUE.equals(progress.get().getIsCompleted());
    }
}
