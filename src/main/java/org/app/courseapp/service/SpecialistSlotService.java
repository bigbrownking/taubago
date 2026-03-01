package org.app.courseapp.service;

import org.app.courseapp.dto.request.AddSlotsRequest;
import org.app.courseapp.dto.request.BookSlotRequest;
import org.app.courseapp.dto.request.SetWeeklyScheduleRequest;
import org.app.courseapp.dto.response.*;

import java.util.List;

public interface SpecialistSlotService {
    void addSlots(AddSlotsRequest request, String email);

    void setWeeklySchedule(SetWeeklyScheduleRequest request, String email);

    void deleteSlot(Long slotId, String email);

    List<SpecialistSlotDto> getMySlots(String email);

    WeekSlotsDto getAvailableSlots(Long specialistId);

    List<BookingHistoryDto> getMyBookings(String email);

    BookingConfirmationDto bookSlot(BookSlotRequest request, String email);

}