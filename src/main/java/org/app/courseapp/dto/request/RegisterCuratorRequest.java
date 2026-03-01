package org.app.courseapp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterCuratorRequest {
    @NotBlank
    private String name;
    @NotBlank private String surname;
    @NotBlank @Email
    private String email;
    @NotBlank private String password;
    private String phoneNumber;
}
