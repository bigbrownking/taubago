package org.app.courseapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseReviewDto {

    private Long courseId;

    private String title;

    private Double averageRating;

    private String formattedRating;

    private Long reviewCount;

    private String colorCode;
}