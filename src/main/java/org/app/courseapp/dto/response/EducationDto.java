package org.app.courseapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EducationDto {
    private Long id;
    private String institution;
    private String degree;
    private Integer yearFrom;
    private Integer yearTo;
    private boolean verified;
    private String documentUrl;
}
