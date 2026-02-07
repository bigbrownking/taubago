package org.app.courseapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.app.courseapp.model.VideoType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto {
    private Long id;
    private String title;
    private VideoType type;
    private String videoUrl;
    private Long durationSeconds;
    private Long fileSizeBytes;
    private Boolean isCompleted;
    private Long watchedSeconds;
}