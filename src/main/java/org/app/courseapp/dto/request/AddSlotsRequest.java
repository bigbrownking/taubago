package org.app.courseapp.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class AddSlotsRequest {
    @NotNull
    private LocalDate date;
    @NotNull
    @JsonFormat(pattern = "HH:mm")
    @Schema(type = "array", example = "[\"09:00\", \"10:00\", \"14:30\"]")
    private List<LocalTime> times;
}
