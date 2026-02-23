package org.app.courseapp.dto.response;

import lombok.*;
import org.app.courseapp.model.VideoType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto {
    private Long id;
    private String title;
    private VideoType type;
    private Long categoryId;
    private String categoryName;
    private String videoUrl;
    private Long durationSeconds;
    private Long fileSizeBytes;
    private Boolean isCompleted;
    private Long watchedSeconds;
}