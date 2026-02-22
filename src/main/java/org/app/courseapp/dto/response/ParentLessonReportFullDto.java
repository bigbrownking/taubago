package org.app.courseapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ParentLessonReportFullDto {
    private Long parentId;
    private String parentName;
    private String parentEmail;

    private Long lessonId;
    private String lessonTitle;
    private Integer dayNumber;

    private Integer childReactionRating;
    private String comment;
    private LocalDateTime reportCreatedAt;
    private LocalDateTime reportUpdatedAt;

    private List<VideoDto> homeworkVideos;
}
