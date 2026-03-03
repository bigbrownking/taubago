package org.app.courseapp.dto.response;

import lombok.Builder;
import lombok.Data;
import org.app.courseapp.model.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class BookingHistoryDto {
    private Long bookingId;
    private Long specialistId;
    private String specialistProfilePictureUrl;
    private String specialistProfession;
    private String specialistName;
    private LocalDate date;
    private LocalTime time;
    private LocalDateTime bookedAt;
    private BookingStatus status;
}
