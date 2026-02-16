package org.app.courseapp.repository;

import org.app.courseapp.model.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {


    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);

    Optional<ReviewLike> findByUserIdAndReviewId(Long userId, Long reviewId);

    Long countByReviewId(Long reviewId);

    List<ReviewLike> findByUserId(Long userId);
}