package org.app.courseapp.util;

import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.response.*;
import org.app.courseapp.dto.response.userProfile.*;
import org.app.courseapp.model.*;
import org.app.courseapp.model.users.*;
import org.app.courseapp.repository.*;
import org.app.courseapp.service.impl.MinioService;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Mapper {

    private final MinioService minioService;
    private final VideoProgressRepository videoProgressRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final RegistrationAnswerRepository registrationAnswerRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    private final CompletionChecker completionChecker;

    public CourseDto convertCourseToDto(Course course, Long userId) {
        boolean isEnrolled = false;
        boolean isCompleted = false;
        boolean available = true;
        String unavailableReason = null;
        long progress = 0;

        if (userId != null) {
            List<CourseEnrollment> userEnrollments = enrollmentRepository.findByUserId(userId);

            Optional<CourseEnrollment> currentEnrollment = userEnrollments.stream()
                    .filter(e -> e.getCourse().getId().equals(course.getId()))
                    .findFirst();

            isEnrolled = currentEnrollment.isPresent();
            isCompleted = currentEnrollment
                    .map(e -> Boolean.TRUE.equals(e.isCompleted()))
                    .orElse(false);

            if (isEnrolled) {
                progress = calculateCourseProgress(course.getId(), userId);
            }

            boolean hasActiveCourse = userEnrollments.stream()
                    .anyMatch(e -> !Boolean.TRUE.equals(e.isCompleted()));

            boolean previousCompleted = true;
            if (course.getOrder() > 1) {
                Optional<Course> previousCourse = courseRepository.findByOrder(course.getOrder() - 1);
                if (previousCourse.isPresent()) {
                    previousCompleted = userEnrollments.stream()
                            .filter(e -> e.getCourse().getId().equals(previousCourse.get().getId()))
                            .findFirst()
                            .map(e -> Boolean.TRUE.equals(e.isCompleted()))
                            .orElse(false);
                }
            }

            if (isEnrolled) {
                available = true;
            } else if (hasActiveCourse) {
                available = false;
                unavailableReason = "Сначала завершите текущий курс";
            } else if (!previousCompleted) {
                available = false;
                unavailableReason = "Сначала завершите предыдущий курс";
            }
        }

        Double averageRating = reviewRepository.findAverageRatingByCourseId(course.getId());
        Long totalRatings = reviewRepository.countByCourseId(course.getId());

        if (averageRating == null) {
            averageRating = 0.0;
            totalRatings = 0L;
        }

        boolean hasUserRated = false;
        boolean hasUserReviewed = false;
        if (userId != null) {
            hasUserRated = reviewRepository.existsByUserIdAndCourseId(userId, course.getId());
            hasUserReviewed = hasUserRated;
        }

        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .currentProgress(progress)
                .durationDays(course.getDurationDays())
                .createdByEmail(course.getCreatedBy() != null ? course.getCreatedBy().getEmail() : null)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .isEnrolled(isEnrolled)
                .isCompleted(isCompleted)
                .keywords(course.getKeywords())
                .averageRating(averageRating)
                .totalRatings(totalRatings)
                .formattedRating(String.format("%.1f", averageRating))
                .hasUserRated(hasUserRated)
                .hasUserReviewed(hasUserReviewed)
                .available(available)
                .order(course.getOrder())
                .unavailableReason(unavailableReason)
                .build();
    }
    public LessonDto convertLessonToDto(Lesson lesson, Long userId) {
        List<Video> lessonVideos = lesson.getLessonVideos();
        boolean isCompleted = !lessonVideos.isEmpty() &&
                lessonVideos.stream()
                        .allMatch(video -> {
                            Optional<VideoProgress> progress = videoProgressRepository
                                    .findByUserIdAndVideoId(userId, video.getId());
                            return progress.isPresent() && Boolean.TRUE.equals(progress.get().getIsCompleted());
                        });

        return LessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .dayNumber(lesson.getDayNumber())
                .isCompleted(isCompleted)
                .videos(lessonVideos.stream()
                        .map(video -> convertVideoToDto(video, userId))
                        .toList())
                .build();
    }

    public VideoDto convertVideoToDto(Video video, Long userId) {
        String videoUrl = minioService.getPresignedUrl(video.getObjectKey(), 2);

        VideoProgress progress = videoProgressRepository
                .findByUserIdAndVideoId(userId, video.getId())
                .orElse(null);

        return VideoDto.builder()
                .id(video.getId())
                .title(video.getTitle())
                .type(video.getType())
                .categoryId(video.getCategory() != null ? video.getCategory().getId() : null)
                .categoryName(video.getCategory() != null ? video.getCategory().getName() : null)
                .videoUrl(videoUrl)
                .durationSeconds(video.getDurationSeconds())
                .fileSizeBytes(video.getFileSizeBytes())
                .isCompleted(progress != null && progress.getIsCompleted())
                .watchedSeconds(progress != null ? progress.getWatchedSeconds() : 0L)
                .build();
    }

    public RegistrationQuestionDto convertRegistrationQuestionToDto(RegistrationQuestion registrationQuestion){
        return RegistrationQuestionDto.builder()
                .id(registrationQuestion.getId())
                .topic(registrationQuestion.getTopic())
                .question(registrationQuestion.getQuestion())
                .build();
    }

    public List<RegistrationQuestionDto> convertRegistrationQuestionsToDto(List<RegistrationQuestion> registrationQuestions){
        List<RegistrationQuestionDto> result = new ArrayList<>();
        for(RegistrationQuestion registrationQuestion : registrationQuestions){
            result.add(convertRegistrationQuestionToDto(registrationQuestion));
        }
        return result;
    }

    private long calculateCourseProgress(Long courseId, Long userId) {
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByDayNumber(courseId);
        if (lessons.isEmpty()) {
            return 0;
        }

        return lessons.stream()
                .filter(lesson -> !lesson.getLessonVideos().isEmpty())
                .filter(lesson -> completionChecker.isLessonCompleted(lesson, userId))
                .count();
    }
    public BaseUserProfileDto convertToProfileDto(User user) {
        if (user instanceof Parent) {
            return convertParentToDto((Parent) user);
        } else if (user instanceof Administrator) {
            return convertAdministratorToDto((Administrator) user);
        } else if (user instanceof Specialist) {
            return convertSpecialistToDto((Specialist) user);
        }
        throw new RuntimeException("Unknown user type");
    }

    public ParentProfileDto convertParentToDto(Parent parent) {
        ParentProfileDto dto = new ParentProfileDto();
        dto.setId(parent.getId());
        dto.setEmail(parent.getEmail());
        dto.setActive(parent.isActive());
        dto.setCreatedDate(parent.getCreatedDate());
        dto.setRoles(parent.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()));
        dto.setUserType("PARENT");
        dto.setName(parent.getName());
        dto.setSurname(parent.getSurname());
        dto.setPhoneNumber(parent.getPhoneNumber());
        dto.setProfilePictureUrl(parent.getProfilePictureUrl());

        // Children
        List<ChildDto> children = parent.getChildren().stream()
                .filter(Child::isActive)
                .map(ChildDto::fromEntity)
                .collect(Collectors.toList());
        dto.setChildren(children);
        dto.setTotalChildren(children.size());

        // Registration stats
        List<RegistrationAnswer> answers = registrationAnswerRepository.findByParentId(parent.getId());
        if (!answers.isEmpty()) {
            long positiveCount = answers.stream().filter(RegistrationAnswer::getAnswer).count();
            long negativeCount = answers.size() - positiveCount;
            double percentage = (positiveCount * 100.0) / answers.size();

            ParentProfileDto.RegistrationStats stats = ParentProfileDto.RegistrationStats.builder()
                    .totalQuestions(answers.size())
                    .positiveAnswers((int) positiveCount)
                    .negativeAnswers((int) negativeCount)
                    .positivePercentage(Math.round(percentage * 10.0) / 10.0)
                    .build();
            dto.setRegistrationStats(stats);
        }

        return dto;
    }

    public AdministratorProfileDto convertAdministratorToDto(Administrator admin) {
        AdministratorProfileDto dto = new AdministratorProfileDto();
        dto.setId(admin.getId());
        dto.setEmail(admin.getEmail());
        dto.setActive(admin.isActive());
        dto.setCreatedDate(admin.getCreatedDate());
        dto.setRoles(admin.getRoles().stream()
                .map(UserRole::getName)
                .collect(Collectors.toSet()));
        dto.setUserType("ADMINISTRATOR");
        dto.setName(admin.getName());
        dto.setSurname(admin.getSurname());
        dto.setPhoneNumber(admin.getPhoneNumber());
        return dto;
    }

    public SpecialistProfileDto convertSpecialistToDto(Specialist specialist) {
        SpecialistProfileDto dto = new SpecialistProfileDto();
        dto.setId(specialist.getId());
        dto.setEmail(specialist.getEmail());
        dto.setActive(specialist.isActive());
        dto.setCreatedDate(specialist.getCreatedDate());
        dto.setRoles(specialist.getRoles().stream()
                .map(UserRole::getName)
                .collect(Collectors.toSet()));
        dto.setUserType("SPECIALIST");
        dto.setName(specialist.getName());
        dto.setSurname(specialist.getSurname());
        dto.setSpecialization(specialist.getSpecialization());
        dto.setPhoneNumber(specialist.getPhoneNumber());
        return dto;
    }

    public String resolveUserName(User user) {
        if (user == null) return "Неизвестный";
        if (user.isDeleted()) return "Удалённый пользователь";
        if (user instanceof Parent p) return p.getName() + " " + p.getSurname();
        if (user instanceof Curator c) return c.getName() + " " + c.getSurname();
        if (user instanceof Specialist s) return s.getName() + " " + s.getSurname();
        if (user instanceof Administrator a) return a.getName() + " " + a.getSurname();
        return user.getEmail();
    }

    public ReviewDto convertToReviewDto(CourseReview review, Long currentUserId) {
        boolean likedByCurrentUser = false;
        if (currentUserId != null) {
            likedByCurrentUser = reviewLikeRepository
                    .existsByUserIdAndReviewId(currentUserId, review.getId());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("ru"));
        String formattedDate = review.getCreatedAt().format(formatter);

        User reviewer = review.getUser();
        User realReviewer = userRepository.findById(reviewer.getId()).orElse(reviewer);
        String userName = resolveUserName(realReviewer);

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
}