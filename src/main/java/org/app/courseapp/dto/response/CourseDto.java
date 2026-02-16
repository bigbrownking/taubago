package org.app.courseapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


 * Updated CourseDto with rating information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {

    private Long id;
    private String title;
    private String description;
    private String month;
    private Integer durationDays;
    private String createdByEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isEnrolled;

    // NEW: Rating fields
    private Double averageRating;
    private Long totalRatings;
    private String formattedRating;
    private Boolean hasUserRated;
    private Boolean hasUserReviewed;
}