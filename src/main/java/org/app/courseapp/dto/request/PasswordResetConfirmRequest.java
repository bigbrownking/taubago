package org.app.courseapp.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Confirm Password Reset Request")
public class PasswordResetConfirmRequest {

    @NotBlank(message = "Token is required")
    @Schema(description = "Password reset token from email",
            example = "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!?.*-]).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    @Schema(description = "New password", example = "NewPassword123!")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    @Schema(description = "Confirm new password", example = "NewPassword123!")
    private String confirmPassword;
}
