package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.dto.request.CreateRatingRequest;
import org.app.courseapp.dto.request.CreateReviewRequest;
import org.app.courseapp.dto.response.CourseRatingStatsDto;
import org.app.courseapp.dto.response.CourseReviewDto;
import org.app.courseapp.dto.response.ReviewDto;
import org.app.courseapp.model.*;
import org.app.courseapp.model.users.Administrator;
import org.app.courseapp.model.users.Parent;
import org.app.courseapp.model.users.Specialist;
import org.app.courseapp.model.users.User;
import org.app.courseapp.repository.*;
import org.app.courseapp.service.RatingService;
import org.app.courseapp.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {

    private final CourseReviewRepository reviewRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final CourseRepository courseRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void rateCourse(CreateRatingRequest request) {
        User currentUser = userService.getCurrentUser();

        if (!(currentUser instanceof Parent)) {
            throw new RuntimeException("Only parents can rate courses");
        }

        if (!enrollmentRepository.existsByUserIdAndCourseId(currentUser.getId(), request.getCourseId())) {
            throw new RuntimeException("You must be enrolled in the course to rate it");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Optional<CourseReview> existingReview = reviewRepository
                .findByUserIdAndCourseId(currentUser.getId(), request.getCourseId());

        CourseReview review;
        if (existingReview.isPresent()) {
            review = existingReview.get();
            review.setRating(request.getRating());
            // reviewText остается как есть (может быть null или старый текст)
            log.info("Parent {} updated rating for course {} to {}",
                    currentUser.getEmail(), course.getTitle(), request.getRating());
        } else {
            review = new CourseReview();
            review.setUser(currentUser);
            review.setCourse(course);
            review.setRating(request.getRating());
            review.setReviewText(null); // Только рейтинг, без текста
            review.setLikeCount(0);
            log.info("Parent {} rated course {} with {} stars",
                    currentUser.getEmail(), course.getTitle(), request.getRating());
        }

        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public ReviewDto reviewCourse(CreateReviewRequest request) {
        User currentUser = userService.getCurrentUser();

        if (!(currentUser instanceof Parent)) {
            throw new RuntimeException("Only parents can review courses");
        }

        if (!enrollmentRepository.existsByUserIdAndCourseId(currentUser.getId(), request.getCourseId())) {
            throw new RuntimeException("You must be enrolled in the course to review it");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Optional<CourseReview> existingReview = reviewRepository
                .findByUserIdAndCourseId(currentUser.getId(), request.getCourseId());

        CourseReview review;
        if (existingReview.isPresent()) {
            review = existingReview.get();
            review.setRating(request.getRating());
            review.setReviewText(request.getReviewText());
            log.info("Parent {} updated review for course {}", currentUser.getEmail(), course.getTitle());
        } else {
            review = new CourseReview();
            review.setUser(currentUser);
            review.setCourse(course);
            review.setRating(request.getRating());
            review.setReviewText(request.getReviewText());
            review.setLikeCount(0);
            log.info("Parent {} created review for course {}", currentUser.getEmail(), course.getTitle());
        }

        review = reviewRepository.save(review);
        return convertToReviewDto(review, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseRatingStatsDto getCourseRatingStats(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Теперь только один источник - reviewRepository
        Double averageRating = reviewRepository.findAverageRatingByCourseId(courseId);
        Long totalRatings = reviewRepository.countByCourseId(courseId);

        if (averageRating == null) {
            averageRating = 0.0;
            totalRatings = 0L;
        }

        // Count ratings by star level
        List<CourseReview> allReviews = reviewRepository.findByCourseId(courseId);
        Map<Integer, Long> ratingCounts = allReviews.stream()
                .collect(Collectors.groupingBy(CourseReview::getRating, Collectors.counting()));

        return CourseRatingStatsDto.builder()
                .averageRating(averageRating)
                .totalRatings(totalRatings)
                .formattedRating(String.format("%.1f", averageRating))
                .fiveStarCount(ratingCounts.getOrDefault(5, 0L))
                .fourStarCount(ratingCounts.getOrDefault(4, 0L))
                .threeStarCount(ratingCounts.getOrDefault(3, 0L))
                .twoStarCount(ratingCounts.getOrDefault(2, 0L))
                .oneStarCount(ratingCounts.getOrDefault(1, 0L))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getCourseReviews(Long courseId, Pageable pageable) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
            // Anonymous user
        }

        Long userId = currentUser != null ? currentUser.getId() : null;

        // Фильтруем только отзывы С ТЕКСТОМ для отображения
        Page<CourseReview> reviews = reviewRepository
                .findByCourseIdAndReviewTextIsNotNullOrderByLikeCountDescCreatedAtDesc(courseId, pageable);

        return reviews.map(review -> convertToReviewDto(review, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDto> getAllReviews(Pageable pageable) {
        User currentUser = null;
        try {
            currentUser = userService.getCurrentUser();
        } catch (Exception e) {
            // Anonymous user
        }

        Long userId = currentUser != null ? currentUser.getId() : null;

        // Только отзывы с текстом
        Page<CourseReview> reviews = reviewRepository
                .findAllByReviewTextIsNotNullOrderByLikeCountDescCreatedAtDesc(pageable);

        return reviews.map(review -> convertToReviewDto(review, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseReviewDto> getAllCoursesWithRatings() {
        List<Course> courses = courseRepository.findAll();

        String[] colors = {
                "#4CAF50", "#2196F3", "#9C27B0", "#FF9800",
                "#F44336", "#00BCD4", "#FFEB3B", "#795548"
        };

        List<CourseReviewDto> result = new ArrayList<>();

        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);

            // Один источник данных
            Double averageRating = reviewRepository.findAverageRatingByCourseId(course.getId());
            Long reviewCount = reviewRepository.countByCourseId(course.getId());

            if (averageRating == null) {
                averageRating = 0.0;
                reviewCount = 0L;
            }

            CourseReviewDto dto = CourseReviewDto.builder()
                    .courseId(course.getId())
                    .title(course.getTitle())
                    .averageRating(averageRating)
                    .formattedRating(String.format("%.1f", averageRating))
                    .reviewCount(reviewCount)
                    .colorCode(colors[i % colors.length])
                    .month(course.getMonth() != null ? course.getMonth().getDisplayName() : null)
                    .build();

            result.add(dto);
        }

        return result;
    }

    @Override
    @Transactional
    public void toggleReviewLike(Long reviewId) {
        User currentUser = userService.getCurrentUser();
        CourseReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        Optional<ReviewLike> existingLike = reviewLikeRepository
                .findByUserIdAndReviewId(currentUser.getId(), reviewId);

        if (existingLike.isPresent()) {
            reviewLikeRepository.delete(existingLike.get());
            review.setLikeCount(Math.max(0, review.getLikeCount() - 1));
            reviewRepository.save(review);
            log.info("User {} unliked review {}", currentUser.getEmail(), reviewId);
        } else {
            ReviewLike like = new ReviewLike();
            like.setUser(currentUser);
            like.setReview(review);
            reviewLikeRepository.save(like);

            review.incrementLikeCount();
            reviewRepository.save(review);
            log.info("User {} liked review {}", currentUser.getEmail(), reviewId);
        }
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        User currentUser = userService.getCurrentUser();
        CourseReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUser().getId().equals(currentUser.getId())
                && !currentUser.hasRole("ROLE_ADMIN")) {
            throw new RuntimeException("Access denied: You can only delete your own reviews");
        }

        reviewRepository.delete(review);
        log.info("Review {} deleted by user {}", reviewId, currentUser.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserRatedCourse(Long courseId) {
        try {
            User currentUser = userService.getCurrentUser();
            return reviewRepository.existsByUserIdAndCourseId(currentUser.getId(), courseId);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedCourse(Long courseId) {
        try {
            User currentUser = userService.getCurrentUser();
            // Проверяем, что есть запись И есть текст отзыва
            Optional<CourseReview> review = reviewRepository
                    .findByUserIdAndCourseId(currentUser.getId(), courseId);
            return review.isPresent() && review.get().getReviewText() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private ReviewDto convertToReviewDto(CourseReview review, Long currentUserId) {
        boolean likedByCurrentUser = false;
        if (currentUserId != null) {
            likedByCurrentUser = reviewLikeRepository
                    .existsByUserIdAndReviewId(currentUserId, review.getId());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("ru"));
        String formattedDate = review.getCreatedAt().format(formatter);

        String userName = getUserDisplayName(review.getUser());

        return ReviewDto.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(userName)
                .courseId(review.getCourse().getId())
                .courseTitle(review.getCourse().getTitle())
                .rating(review.getRating())
                .reviewText(review.getReviewText())
                .likeCount(review.getLikeCount())
                .likedByCurrentUser(likedByCurrentUser)
                .createdAt(review.getCreatedAt())
                .formattedDate(formattedDate)
                .build();
    }

    private String getUserDisplayName(User user) {
        if (user instanceof Parent) {
            Parent parent = (Parent) user;
            return parent.getName() + " " + parent.getSurname();
        } else if (user instanceof Administrator) {
            Administrator admin = (Administrator) user;
            return admin.getName() + " " + admin.getSurname();
        } else if (user instanceof Specialist) {
            Specialist specialist = (Specialist) user;
            return specialist.getName() + " " + specialist.getSurname();
        }
        return user.getEmail();
    }
}