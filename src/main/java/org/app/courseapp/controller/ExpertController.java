package org.app.courseapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.request.*;
import org.app.courseapp.dto.response.*;
import org.app.courseapp.service.SpecialistService;
import org.app.courseapp.service.SpecialistSlotService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @PatchMapping("/me/about")
    @PreAuthorize("hasRole('ROLE_SPECIALIST')")
    @Operation(summary = "Update specialist about section")
    public ResponseEntity<Void> updateAbout(@Valid @RequestBody UpdateAboutRequest request) {
        specialistService.updateAbout(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me/profession")
    @PreAuthorize("hasRole('ROLE_SPECIALIST')")
    @Operation(summary = "Update specialist profession")
    public ResponseEntity<Void> updateProfession(@RequestParam String profession) {
        specialistService.updateProfession(profession);
        return ResponseEntity.ok().build();
    }
    // ─── Specialist: education ────────────────────────────────────────────────

    @PostMapping("/me/education")
    @PreAuthorize("hasRole('ROLE_SPECIALIST')")
    @Operation(summary = "Add education entry")
    public ResponseEntity<EducationDto> addEducation(@Valid @RequestBody AddEducationRequest request) {
        return ResponseEntity.ok(specialistService.addEducation(request));
    }

    @DeleteMapping("/me/education/{educationId}")
    @PreAuthorize("hasRole('ROLE_SPECIALIST')")
    @Operation(summary = "Delete education entry")
    public ResponseEntity<Void> deleteEducation(@PathVariable Long educationId) {
        specialistService.deleteEducation(educationId);
        return ResponseEntity.noContent().build();
    }

    // ─── Specialist: work experience ──────────────────────────────────────────

    @PostMapping("/me/work-experience")
    @PreAuthorize("hasRole('ROLE_SPECIALIST')")
    @Operation(summary = "Add work experience entry")
    public ResponseEntity<WorkExperienceDto> addWorkExperience(@Valid @RequestBody AddWorkExperienceRequest request) {
        return ResponseEntity.ok(specialistService.addWorkExperience(request));
    }

    @DeleteMapping("/me/work-experience/{expId}")
    @PreAuthorize("hasRole('ROLE_SPECIALIST')")
    @Operation(summary = "Delete work experience entry")
    public ResponseEntity<Void> deleteWorkExperience(@PathVariable Long expId) {
        specialistService.deleteWorkExperience(expId);
        return ResponseEntity.noContent().build();
    }

    // ─── Specialist: certificates ─────────────────────────────────────────────

    @PostMapping("/me/certificates")
    @PreAuthorize("hasRole('ROLE_SPECIALIST')")
    @Operation(summary = "Add certificate")
    public ResponseEntity<CertificateDto> addCertificate(@Valid @RequestBody AddCertificateRequest request) {
        return ResponseEntity.ok(specialistService.addCertificate(request));
    }

    @DeleteMapping("/me/certificates/{certId}")
    @PreAuthorize("hasRole('ROLE_SPECIALIST')")
    @Operation(summary = "Delete certificate")
    public ResponseEntity<Void> deleteCertificate(@PathVariable Long certId) {
        specialistService.deleteCertificate(certId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/me/certificates/{certId}/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_SPECIALIST')")
    @Operation(summary = "Upload certificate document")
    public ResponseEntity<CertificateDto> uploadCertificateDocument(
            @PathVariable Long certId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(specialistService.uploadCertificateDocument(certId, file));
    }
}