package org.app.courseapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.dto.request.UpdateProfileRequest;
import org.app.courseapp.dto.response.userProfile.BaseUserProfileDto;
import org.app.courseapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/me")
    @Operation(summary = "Update my profile", description = "Update current user's profile information")
    public ResponseEntity<BaseUserProfileDto> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        log.info("Updating profile for {}", email);
        return ResponseEntity.ok(userService.updateMyProfile(request));
    }
}