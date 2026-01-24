package org.app.courseapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.request.*;
import org.app.courseapp.dto.response.JwtResponse;
import org.app.courseapp.dto.response.PasswordResetResponse;
import org.app.courseapp.dto.response.SignUpResponse;
import org.app.courseapp.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication Management", description = "APIs for auth management operations")
public class AuthController {
    private final AuthService authService;


    @PostMapping("/signup")
    @Operation(summary = "Register a new user", description = "Creates a new user account with provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = SignUpResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<SignUpResponse> signup(
            @Valid @RequestBody SignUpRequest signUpRequest) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.signup(signUpRequest));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = """
                Allows two authentication methods:
                1) Login using email + password
                2) Login using email + 4-digit pincode
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email, password or pincode"
            )
    })
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Refresh token is required"
            )
    })
    public ResponseEntity<JwtResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/password-reset/request")
    @Operation(
            summary = "Request password reset",
            description = "Sends a password reset email with a reset token to the user's email address"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset email sent successfully",
                    content = @Content(schema = @Schema(implementation = PasswordResetResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found with provided email"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid email format"
            )
    })
    public ResponseEntity<PasswordResetResponse> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {

        PasswordResetResponse response = authService.requestPasswordReset(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password-reset/confirm")
    @Operation(
            summary = "Confirm password reset",
            description = "Resets the user's password using the token sent via email"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset successfully",
                    content = @Content(schema = @Schema(implementation = PasswordResetResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid token, expired token, or passwords don't match"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Token not found"
            )
    })
    public ResponseEntity<PasswordResetResponse> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request) {

        PasswordResetResponse response = authService.confirmPasswordReset(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/password-reset/validate")
    @Operation(
            summary = "Validate password reset token",
            description = "Checks if the password reset token is valid and not expired"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token validation result"
            )
    })
    public ResponseEntity<Boolean> validateResetToken(
            @RequestParam String token) {

        boolean isValid = authService.validateResetToken(token);
        return ResponseEntity.ok(isValid);
    }
}
