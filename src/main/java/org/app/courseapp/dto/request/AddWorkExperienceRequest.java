package org.app.courseapp.dto.request;

import lombok.Getter;

@Getter
public class AddWorkExperienceRequest {
    private String organization;
    private String position;
    private Integer yearFrom;
    private Integer yearTo;
    private boolean current;
}
