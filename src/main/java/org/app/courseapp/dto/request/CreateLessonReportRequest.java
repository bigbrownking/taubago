package org.app.courseapp.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateLessonReportRequest {
    @NotNull
    @Min(1) @Max(5)
    private Integer childReactionRating;

    @Size(max = 2000)
    private String comment;
}