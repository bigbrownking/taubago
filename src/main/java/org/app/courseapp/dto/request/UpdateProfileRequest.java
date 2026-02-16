package org.app.courseapp.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Surname is required")
    @Size(min = 2, max = 100, message = "Surname must be between 2 and 100 characters")
    private String surname;

    @Nullable
    @Pattern(
            regexp = "^\\+?[1-9][\\d\\s()-]{7,20}$",
            message = "Phone number must be valid"
    )
    private String phoneNumber;

    @Nullable
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!?.*-]).+$",
            message = "Password must contain at least one uppercase, lowercase, number, and special character"
    )
    private String password;

    @Nullable
    private String specialization;
}