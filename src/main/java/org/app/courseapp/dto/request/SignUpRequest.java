package org.app.courseapp.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SignUpRequest {

    // Данные родителя
    @NotBlank(message = "Parent name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String parentName;

    @NotBlank(message = "Parent surname is required")
    @Size(min = 2, max = 100, message = "Surname must be between 2 and 100 characters")
    private String parentSurname;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+?[1-9][\\d\\s()-]{7,20}$",
            message = "Phone number must be valid"
    )
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!?.*-]).+$",
            message = "Password must contain at least one uppercase, lowercase, number, and special character"
    )
    private String password;

    @Nullable
    private String pinCode;

    @NotNull(message = "Registration answers are required")
    @Size(min = 1, message = "At least one answer is required")
    @Valid
    private List<QuestionAnswer> registrationAnswers;

    @Nullable
    @Valid
    private ChildData firstChild;

    @Getter
    @Setter
    public static class QuestionAnswer {
        @NotNull(message = "Question ID is required")
        private Long questionId;

        @NotNull(message = "Answer is required")
        private Boolean answer;
    }

    @Getter
    @Setter
    public static class ChildData {
        @NotBlank(message = "Child name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;

        @NotBlank(message = "Child surname is required")
        @Size(min = 2, max = 100, message = "Surname must be between 2 and 100 characters")
        private String surname;

        @NotNull(message = "Age is required")
        @Min(value = 1, message = "Age must be at least 1")
        @Max(value = 18, message = "Age must not exceed 18")
        private Integer age;

        @Nullable
        private Long diagnosisId;
    }
}