package org.app.courseapp.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Data
public class SetWeeklyScheduleRequest {
    @NotNull
    private List<DayOfWeek> workDays;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    private Integer slotDurationMinutes;

}
