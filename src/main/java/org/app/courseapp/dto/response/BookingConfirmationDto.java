package org.app.courseapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class BookingConfirmationDto {
    private Long bookingId;
    private String specialistName;
    private LocalDate date;
    private LocalTime time;
    private boolean isFreeSession;
    private LocalDateTime bookedAt;
}