package org.app.courseapp.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateReviewRequest {
    @Min(1) @Max(5)
    private Integer rating;
    @Size(max = 1000)
    private String reviewText;
}
