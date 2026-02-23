package org.app.courseapp.util;

import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.response.CourseDto;
import org.app.courseapp.dto.response.LessonDto;
import org.app.courseapp.dto.response.RegistrationQuestionDto;
import org.app.courseapp.dto.response.VideoDto;
import org.app.courseapp.dto.response.userProfile.*;
import org.app.courseapp.model.*;
import org.app.courseapp.model.users.*;
import org.app.courseapp.repository.*;
import org.app.courseapp.service.impl.MinioService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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
    private final RegistrationAnswerRepository registrationAnswerRepository;

    public CourseDto convertCourseToDto(Course course, Long userId) {
        boolean isEnrolled = false;
        boolean available = true;
        String unavailableReason = null;

        if (userId != null) {
            isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, course.getId());

            List<CourseEnrollment> userEnrollments = enrollmentRepository.findByUserId(userId);

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
                .durationDays(course.getDurationDays())
                .createdByEmail(course.getCreatedBy() != null ? course.getCreatedBy().getEmail() : null)
                .keywords(course.getKeywords())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .isEnrolled(isEnrolled)
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
        LessonDto dto = LessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .dayNumber(lesson.getDayNumber())
                .build();

        List<VideoDto> videoDtos = lesson.getVideos().stream()
                .map(video -> convertVideoToDto(video, userId))
                .toList();
        dto.setVideos(videoDtos);

        return dto;
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
        dto.setExperienceYears(specialist.getExperienceYears());
        dto.setPhoneNumber(specialist.getPhoneNumber());
        return dto;
    }
}