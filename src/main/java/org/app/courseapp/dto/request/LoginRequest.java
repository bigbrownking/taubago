package org.app.courseapp.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotNull(message = "Email is required")
    private String email;
    private String password;
    private String pincode;
}
