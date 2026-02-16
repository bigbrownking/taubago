package org.app.courseapp.repository;

import org.app.courseapp.model.CourseReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    Optional<CourseReview> findByUserIdAndCourseId(Long userId, Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseReview> findByCourseId(Long courseId);

    // Только отзывы с текстом
    Page<CourseReview> findByCourseIdAndReviewTextIsNotNullOrderByLikeCountDescCreatedAtDesc(
            Long courseId, Pageable pageable);

    Page<CourseReview> findAllByReviewTextIsNotNullOrderByLikeCountDescCreatedAtDesc(
            Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM CourseReview r WHERE r.course.id = :courseId")
    Double findAverageRatingByCourseId(Long courseId);

    Long countByCourseId(Long courseId);
}