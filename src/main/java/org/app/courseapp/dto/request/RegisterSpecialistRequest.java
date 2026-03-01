package org.app.courseapp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RegisterSpecialistRequest {
    @NotBlank private String name;
    @NotBlank private String surname;
    @NotBlank @Email
    private String email;
    @NotBlank
    private String password;
    private String phoneNumber;
    private Integer experienceYears;
    private String photoUrl;
    private String telegramUrl;
    private boolean hasFreeSession;
    private Integer pricePerHour;
    private Double rating;
    private List<Long> specializationIds;
}
