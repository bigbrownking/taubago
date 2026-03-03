package org.app.courseapp.dto.request;

import lombok.Getter;

@Getter
public class AddEducationRequest {
    private String institution;
    private String degree;
    private Integer yearFrom;
    private Integer yearTo;
}
