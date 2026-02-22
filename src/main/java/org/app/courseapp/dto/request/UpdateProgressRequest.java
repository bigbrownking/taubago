package org.app.courseapp.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProgressRequest {
    @NotNull
    private Long watchedSeconds;
}
