package org.app.courseapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    private Long id;

    private Long userId;

    private String userName;

    private String userProfilePictureUrl;

    private Long courseId;

    private String courseTitle;

    private Integer rating;

    private String reviewText;

    private Integer likeCount;

    private Boolean likedByCurrentUser;

    private LocalDateTime createdAt;

    private String formattedDate;
}