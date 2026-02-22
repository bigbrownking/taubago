package org.app.courseapp.repository;

import org.app.courseapp.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseIdOrderByDayNumber(Long courseId);
    @Query("""
                SELECT l FROM Lesson l
                WHERE l.course.id = :courseId
                AND l.id NOT IN (
                    SELECT vp.video.lesson.id FROM VideoProgress vp
                    WHERE vp.user.id = :userId
                    AND vp.isCompleted = true
                )
                ORDER BY l.dayNumber ASC
                LIMIT 1
            """)
    Optional<Lesson> findFirstIncompleteLessonByCourseAndUser(
            @Param("courseId") Long courseId,
            @Param("userId") Long userId
    );
}