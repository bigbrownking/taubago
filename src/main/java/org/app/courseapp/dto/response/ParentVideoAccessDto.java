package org.app.courseapp.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentVideoAccessDto {
    private Long parentId;
    private String parentName;
    private String parentEmail;
    private List<VideoCategoryDto> allowedCategories;
    private List<VideoCategoryDto> allCategories;
}