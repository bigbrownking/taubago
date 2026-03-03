package org.app.courseapp.service;

import org.app.courseapp.dto.request.CreateRatingRequest;
import org.app.courseapp.dto.request.CreateReviewRequest;
import org.app.courseapp.dto.request.CreateSpecialistReviewRequest;
import org.app.courseapp.dto.request.UpdateReviewRequest;
import org.app.courseapp.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RatingService {

    void rateCourse(CreateRatingRequest request);


    ReviewDto reviewCourse(CreateReviewRequest request);

    CourseRatingStatsDto getCourseRatingStats(Long courseId);

    Page<ReviewDto> getCourseReviews(Long courseId, Pageable pageable);

    Page<ReviewDto> getAllReviews(Pageable pageable);
    List<CourseReviewDto> getAllCoursesWithRatings();

    void toggleReviewLike(Long reviewId);
    void toggleSpecialistReviewLike(Long reviewId);

    void deleteReview(Long reviewId);

    boolean hasUserRatedCourse(Long courseId);

    boolean hasUserReviewedCourse(Long courseId);


    // Specialist reviews
    SpecialistReviewDto reviewSpecialist(CreateSpecialistReviewRequest request);
    void updateSpecialistReview(Long reviewId, UpdateReviewRequest request);
    void deleteSpecialistReview(Long reviewId);
    Page<SpecialistReviewDto> getSpecialistReviews(Long specialistId, Pageable pageable);

    // Course review update
    ReviewDto updateCourseReview(Long reviewId, UpdateReviewRequest request);

    // My reviews
    MyReviewsDto getMyReviews();
}