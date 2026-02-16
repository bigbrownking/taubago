package org.app.courseapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.dto.request.CreateRatingRequest;
import org.app.courseapp.dto.request.CreateReviewRequest;
import org.app.courseapp.dto.response.CourseRatingStatsDto;
import org.app.courseapp.dto.response.CourseReviewDto;
import org.app.courseapp.dto.response.ReviewDto;
import org.app.courseapp.service.RatingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ratings & Reviews", description = "APIs for course ratings and reviews")
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/rate")
    @Operation(summary = "Rate a course", description = "Quick rating (1-5 stars) without review text")
    public ResponseEntity<Void> rateCourse(@Valid @RequestBody CreateRatingRequest request) {
        log.info("Received rating request for course {}", request.getCourseId());
        ratingService.rateCourse(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/review")
    @Operation(summary = "Review a course", description = "Full review with rating and text")
    public ResponseEntity<ReviewDto> reviewCourse(@Valid @RequestBody CreateReviewRequest request) {
        log.info("Received review request for course {}", request.getCourseId());
        ReviewDto review = ratingService.reviewCourse(request);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/course/{courseId}/stats")
    @Operation(summary = "Get course rating stats", description = "Get average rating and distribution")
    public ResponseEntity<CourseRatingStatsDto> getCourseRatingStats(@PathVariable Long courseId) {
        log.info("Fetching rating stats for course {}", courseId);
        CourseRatingStatsDto stats = ratingService.getCourseRatingStats(courseId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/course/{courseId}/reviews")
    @Operation(summary = "Get course reviews", description = "Get all reviews with text for a course")
    public ResponseEntity<Page<ReviewDto>> getCourseReviews(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching reviews for course {} (page {}, size {})", courseId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDto> reviews = ratingService.getCourseReviews(courseId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/reviews")
    @Operation(summary = "Get all reviews", description = "Get all reviews with text across all courses")
    public ResponseEntity<Page<ReviewDto>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching all reviews (page {}, size {})", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDto> reviews = ratingService.getAllReviews(pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/courses-with-ratings")
    @Operation(summary = "Get courses with ratings", description = "Get all courses with their average ratings")
    public ResponseEntity<List<CourseReviewDto>> getAllCoursesWithRatings() {
        log.info("Fetching all courses with ratings");
        List<CourseReviewDto> courses = ratingService.getAllCoursesWithRatings();
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/review/{reviewId}/like")
    @Operation(summary = "Like/unlike review", description = "Toggle like on a review")
    public ResponseEntity<Void> toggleReviewLike(@PathVariable Long reviewId) {
        log.info("Toggling like for review {}", reviewId);
        ratingService.toggleReviewLike(reviewId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/review/{reviewId}")
    @Operation(summary = "Delete review", description = "Delete a review (own or admin)")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        log.info("Deleting review {}", reviewId);
        ratingService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/course/{courseId}/has-reviewed")
    @Operation(summary = "Check if reviewed", description = "Check if user has rated or reviewed the course")
    public ResponseEntity<Boolean> hasUserReviewedCourse(@PathVariable Long courseId) {
        boolean hasReviewed = ratingService.hasUserRatedCourse(courseId); // используем hasUserRatedCourse
        return ResponseEntity.ok(hasReviewed);
    }

    @GetMapping("/course/{courseId}/has-review-text")
    @Operation(summary = "Check if has review text", description = "Check if user has written review text")
    public ResponseEntity<Boolean> hasUserWrittenReviewText(@PathVariable Long courseId) {
        boolean hasReviewText = ratingService.hasUserReviewedCourse(courseId);
        return ResponseEntity.ok(hasReviewText);
    }
}