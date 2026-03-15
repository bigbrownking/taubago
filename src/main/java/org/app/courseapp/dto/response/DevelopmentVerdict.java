package org.app.courseapp.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DevelopmentVerdict {
    private String verdict;
    private String analysis;
    private String recommendation;
    private String conclusion;
    private int positiveCount;
    private int totalCount;
}
