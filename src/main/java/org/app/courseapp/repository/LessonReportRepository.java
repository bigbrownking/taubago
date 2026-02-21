package org.app.courseapp.repository;

import org.app.courseapp.model.LessonReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonReportRepository extends JpaRepository<LessonReport, Long> {
    Optional<LessonReport> findByLessonIdAndParentId(Long lessonId, Long parentId);
    List<LessonReport> findByLessonId(Long lessonId);
    List<LessonReport> findByParentId(Long parentId);
}
