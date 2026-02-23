package org.app.courseapp.dto.response;

import lombok.*;
import org.app.courseapp.model.VideoCategory;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoCategoryDto {
    private Long id;
    private String name;
    private Boolean hasAccess;

    public static VideoCategoryDto fromEntity(VideoCategory category) {
        return VideoCategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static VideoCategoryDto fromEntity(VideoCategory category, boolean hasAccess) {
        return VideoCategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .hasAccess(hasAccess)
                .build();
    }
}