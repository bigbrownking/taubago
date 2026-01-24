package org.app.courseapp.service;

import org.app.courseapp.dto.request.*;
import org.app.courseapp.dto.response.JwtResponse;
import org.app.courseapp.dto.response.PasswordResetResponse;
import org.app.courseapp.dto.response.SignUpResponse;

public interface AuthService {
    SignUpResponse signup(SignUpRequest request);
    JwtResponse login(LoginRequest request);
    JwtResponse refreshToken(RefreshTokenRequest request);
    PasswordResetResponse requestPasswordReset(PasswordResetRequest request);
    PasswordResetResponse confirmPasswordReset(PasswordResetConfirmRequest request);
    boolean validateResetToken(String token);
}
