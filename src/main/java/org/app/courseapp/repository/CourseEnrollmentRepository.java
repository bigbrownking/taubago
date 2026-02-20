package org.app.courseapp.repository;

import org.app.courseapp.model.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    List<CourseEnrollment> findByUserId(Long userId);
    List<CourseEnrollment> findByCourseId(Long courseId);
    Optional<CourseEnrollment> findByUserIdAndCourseId(Long userId, Long courseId);
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    List<CourseEnrollment> findByUserIdAndCompleted(Long userId, Boolean completed);

}