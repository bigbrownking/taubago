package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.dto.request.AddSlotsRequest;
import org.app.courseapp.dto.request.BookSlotRequest;
import org.app.courseapp.dto.request.SetWeeklyScheduleRequest;
import org.app.courseapp.dto.response.BookingConfirmationDto;
import org.app.courseapp.dto.response.BookingHistoryDto;
import org.app.courseapp.dto.response.SpecialistSlotDto;
import org.app.courseapp.dto.response.WeekSlotsDto;
import org.app.courseapp.model.Booking;
import org.app.courseapp.model.BookingStatus;
import org.app.courseapp.model.SpecialistSlot;
import org.app.courseapp.model.users.Parent;
import org.app.courseapp.model.users.Specialist;
import org.app.courseapp.model.users.User;
import org.app.courseapp.repository.BookingRepository;
import org.app.courseapp.repository.SpecialistSlotRepository;
import org.app.courseapp.repository.UserRepository;
import org.app.courseapp.service.SpecialistSlotService;
import org.app.courseapp.util.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecialistSlotServiceImpl implements SpecialistSlotService {

    private final BookingRepository bookingRepository;
    private final SpecialistSlotRepository slotRepository;
    private final UserRepository userRepository;
    private final Mapper mapper;

    @Override
    @Transactional
    public void addSlots(AddSlotsRequest request, String email) {
        Specialist specialist = getSpecialist(email);

        List<SpecialistSlot> newSlots = request.getTimes().stream()
                .filter(time -> !slotRepository.existsBySpecialistIdAndDateAndTime(
                        specialist.getId(), request.getDate(), time))
                .map(time -> SpecialistSlot.builder()
                        .specialist(specialist)
                        .date(request.getDate())
                        .time(time)
                        .booked(false)
                        .build())
                .toList();

        slotRepository.saveAll(newSlots);
        log.info("Specialist {} added {} slots for {}", email, newSlots.size(), request.getDate());
    }

    @Override
    @Transactional
    public void deleteSlot(Long slotId, String email) {
        Specialist specialist = getSpecialist(email);

        SpecialistSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found: " + slotId));

        if (!slot.getSpecialist().getId().equals(specialist.getId())) {
            throw new RuntimeException("Access denied: slot does not belong to you");
        }

        if (slot.isBooked()) {
            throw new RuntimeException("Cannot delete a booked slot");
        }

        slotRepository.delete(slot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialistSlotDto> getMySlots(String email) {
        Specialist specialist = getSpecialist(email);
        return slotRepository.findBySpecialistIdOrderByDateAscTimeAsc(specialist.getId())
                .stream()
                .map(mapper::convertToSpecialistSlotDto)
                .toList();
    }

    @Override
    @Transactional
    public void setWeeklySchedule(SetWeeklyScheduleRequest request, String email) {
        Specialist specialist = getSpecialist(email);

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusWeeks(4);

        slotRepository.deleteAllFutureUnbookedSlots(specialist.getId(), today);

        List<SpecialistSlot> slots = new ArrayList<>();

        today.datesUntil(endDate).forEach(date -> {
            if (request.getWorkDays().contains(date.getDayOfWeek())) {
                LocalTime time = request.getStartTime();
                while (time.isBefore(request.getEndTime())) {
                    LocalTime finalTime = time;
                    slots.add(SpecialistSlot.builder()
                            .specialist(specialist)
                            .date(date)
                            .time(finalTime)
                            .booked(false)
                            .build());
                    time = time.plusMinutes(request.getSlotDurationMinutes());
                }
            }
        });

        slotRepository.saveAll(slots);
        log.info("Specialist {} updated weekly schedule: {} slots generated", email, slots.size());
    }

    @Override
    @Transactional(readOnly = true)
    public WeekSlotsDto getAvailableSlots(Long specialistId) {
        Specialist specialist = (Specialist) userRepository.findById(specialistId)
                .orElseThrow(() -> new RuntimeException("Specialist not found: " + specialistId));

        LocalDate today = LocalDate.now();
        LocalDate weekEnd = today.with(DayOfWeek.MONDAY).plusDays(6);

        List<SpecialistSlotDto> slots = slotRepository
                .findAvailableSlots(specialistId, today, weekEnd)
                .stream()
                .map(mapper::convertToSpecialistSlotDto)
                .toList();

        return WeekSlotsDto.builder()
                .specialistId(specialistId)
                .specialistName(specialist.getName() + " " + specialist.getSurname())
                .weekDays(today.datesUntil(weekEnd.plusDays(1)).toList())
                .slots(slots)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingHistoryDto> getMyBookings(String email) {
        Parent parent = getParent(email);
        return bookingRepository.findByParentIdOrderByBookedAtDesc(parent.getId())
                .stream()
                .map(mapper::convertToBookingHistoryDto)
                .toList();
    }

    @Override
    @Transactional
    public BookingConfirmationDto bookSlot(BookSlotRequest request, String email) {
        Parent parent = getParent(email);

        SpecialistSlot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new RuntimeException("Slot not found: " + request.getSlotId()));

        if (slot.isBooked()) {
            throw new RuntimeException("Slot is already booked");
        }

        slot.setBooked(true);
        slot.setBookedBy(parent);
        slotRepository.save(slot);

        Booking booking = Booking.builder()
                .parent(parent)
                .slot(slot)
                .bookedAt(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .build();

        bookingRepository.save(booking);
        Specialist specialist = slot.getSpecialist();
            specialist.setSessionCount(specialist.getSessionCount() + 1);
        userRepository.save(specialist);

        log.info("Parent {} booked slot {} with specialist {}",
                email, slot.getId(), slot.getSpecialist().getEmail());

        return mapper.convertToBookingConfirmationDto(booking);
    }

    private Specialist getSpecialist(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        if (!(user instanceof Specialist specialist)) {
            throw new RuntimeException("User is not a specialist");
        }
        return specialist;
    }

    private Parent getParent(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        if (!(user instanceof Parent parent)) {
            throw new RuntimeException("User is not a parent");
        }
        return parent;
    }
}