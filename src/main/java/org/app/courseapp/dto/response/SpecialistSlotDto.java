package org.app.courseapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class SpecialistSlotDto {
    private Long id;
    private LocalDate date;
    private LocalTime time;
    private boolean booked;
}
