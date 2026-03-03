package org.app.courseapp.repository;

import org.app.courseapp.model.SpecialistReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpecialistReviewRepository extends JpaRepository<SpecialistReview, Long> {
    Optional<SpecialistReview> findByUserIdAndSpecialistId(Long userId, Long specialistId);
    boolean existsByUserIdAndSpecialistId(Long userId, Long specialistId);
    Page<SpecialistReview> findBySpecialistIdAndReviewTextIsNotNullOrderByLikeCountDescCreatedAtDesc(Long specialistId, Pageable pageable);
    @Query("SELECT AVG(r.rating) FROM SpecialistReview r WHERE r.specialist.id = :specialistId")
    Double findAverageRatingBySpecialistId(@Param("specialistId") Long specialistId);
    Long countBySpecialistId(Long specialistId);
    List<SpecialistReview> findByUserId(Long userId);
}
