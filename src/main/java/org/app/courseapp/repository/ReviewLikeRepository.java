package org.app.courseapp.repository;

import org.app.courseapp.model.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    // course
    Optional<ReviewLike> findByUserIdAndCourseReviewId(Long userId, Long courseReviewId);
    boolean existsByUserIdAndCourseReviewId(Long userId, Long courseReviewId);

    // specialist
    Optional<ReviewLike> findByUserIdAndSpecialistReviewId(Long userId, Long specialistReviewId);
    boolean existsByUserIdAndSpecialistReviewId(Long userId, Long specialistReviewId);
}