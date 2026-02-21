package org.app.courseapp.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LessonReportDto {
    private Long id;
    private Long lessonId;
    private String lessonTitle;
    private Integer dayNumber;
    private Integer childReactionRating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String parentName;
    private String parentEmail;
}
