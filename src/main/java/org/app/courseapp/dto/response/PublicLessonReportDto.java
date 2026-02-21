package org.app.courseapp.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PublicLessonReportDto {
    private Integer childReactionRating;
    private String comment;
    private LocalDateTime createdAt;
    private String parentName;
}
