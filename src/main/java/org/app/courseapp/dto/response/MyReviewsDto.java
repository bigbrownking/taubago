package org.app.courseapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MyReviewsDto {
    private List<ReviewDto> courseReviews;
    private List<SpecialistReviewDto> specialistReviews;
}
