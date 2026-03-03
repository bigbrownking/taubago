package org.app.courseapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkExperienceDto {
    private Long id;
    private String organization;
    private String position;
    private Integer yearFrom;
    private Integer yearTo;
    private boolean current;
}
