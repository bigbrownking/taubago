package org.app.courseapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.request.BookSlotRequest;
import org.app.courseapp.dto.response.*;
import org.app.courseapp.service.SpecialistService;
import org.app.courseapp.service.SpecialistSlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/experts")
@Tag(name = "Expert Management", description = "APIs for expert management operations")
public class ExpertController {

    private final SpecialistService specialistService;
    private final SpecialistSlotService slotService;

    @GetMapping
    @Operation(summary = "Get all specialists, optionally filtered by specialization")
    public ResponseEntity<List<SpecialistCardDto>> getAll(
            @RequestParam(required = false) Long specializationId
    ) {
        return ResponseEntity.ok(specialistService.getAllSpecialists(specializationId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specialist details by ID")
    public ResponseEntity<SpecialistDetailDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(specialistService.getSpecialistById(id));
    }

    @GetMapping("/{id}/available-slots")
    @Operation(summary = "Get available slots for current week")
    public ResponseEntity<WeekSlotsDto> getAvailableSlots(@PathVariable Long id) {
        return ResponseEntity.ok(slotService.getAvailableSlots(id));
    }

    @GetMapping("/bookings/my")
    @PreAuthorize("hasRole('ROLE_PARENT')")
    @Operation(summary = "Get my booking history")
    public ResponseEntity<List<BookingHistoryDto>> getMyBookings(Authentication authentication) {
        return ResponseEntity.ok(slotService.getMyBookings(authentication.getName()));
    }

    @PostMapping("/booking/book")
    @PreAuthorize("hasRole('ROLE_PARENT')")
    @Operation(summary = "Book a slot")
    public ResponseEntity<BookingConfirmationDto> bookSlot(
            @Valid @RequestBody BookSlotRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(slotService.bookSlot(request, authentication.getName()));
    }
}