package org.app.courseapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.dto.request.ChangePasswordRequest;
import org.app.courseapp.dto.request.UpdateProfileRequest;
import org.app.courseapp.dto.response.userProfile.BaseUserProfileDto;
import org.app.courseapp.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User Management", description = "APIs for user management operations")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Get current user's profile information")
    public ResponseEntity<BaseUserProfileDto> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        log.info("Fetching profile for {}", email);
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update my profile")
    public ResponseEntity<BaseUserProfileDto> updateMyProfile(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            Authentication authentication) throws IOException {
        log.info("Updating profile for {}", authentication.getName());

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName(name);
        request.setSurname(surname);
        request.setPhoneNumber(phoneNumber);

        return ResponseEntity.ok(userService.updateMyProfile(request, photo));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount() {
        userService.deactivateMyAccount();
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(
            @RequestBody ChangePasswordRequest request) {
        userService.changeMyPassword(request);
        return ResponseEntity.ok().build();
    }
}