package org.app.courseapp.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpecialistReviewDto {
    private Long id;
    private Long specialistId;
    private String specialistName;
    private Long userId;
    private String userName;
    private String userProfilePictureUrl;
    private Integer rating;
    private String reviewText;
    private Integer likeCount;
    private boolean likedByMe;
    private String createdAt;
}
