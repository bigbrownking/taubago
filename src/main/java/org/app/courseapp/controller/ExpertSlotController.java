package org.app.courseapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.request.AddSlotsRequest;
import org.app.courseapp.dto.request.SetWeeklyScheduleRequest;
import org.app.courseapp.dto.response.SpecialistSlotDto;
import org.app.courseapp.service.SpecialistSlotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/experts/my/slots")
@Tag(name = "Expert Slots", description = "Expert manages own available slots")
public class ExpertSlotController {

    private final SpecialistSlotService slotService;

    @GetMapping
    @Operation(summary = "Get my slots")
    public ResponseEntity<List<SpecialistSlotDto>> getMySlots(Authentication authentication) {
        return ResponseEntity.ok(slotService.getMySlots(authentication.getName()));
    }

    @PostMapping
    @Operation(summary = "Add available time slots")
    public ResponseEntity<Void> addSlots(
            @Valid @RequestBody AddSlotsRequest request,
            Authentication authentication) {
        slotService.addSlots(request, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{slotId}")
    @Operation(summary = "Delete a slot")
    public ResponseEntity<Void> deleteSlot(
            @PathVariable Long slotId,
            Authentication authentication) {
        slotService.deleteSlot(slotId, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/schedule")
    @Operation(summary = "Set weekly schedule — auto-generates slots",
            description = "Example: Mon-Fri 09:00-19:00 every 60 min for 4 weeks ahead")
    public ResponseEntity<Void> setWeeklySchedule(
            @Valid @RequestBody SetWeeklyScheduleRequest request,
            Authentication authentication) {
        slotService.setWeeklySchedule(request, authentication.getName());
        return ResponseEntity.ok().build();
    }
}
