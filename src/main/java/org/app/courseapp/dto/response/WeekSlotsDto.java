package org.app.courseapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class WeekSlotsDto {
    private Long specialistId;
    private String specialistName;
    private List<LocalDate> weekDays;
    private List<SpecialistSlotDto> slots;
}
