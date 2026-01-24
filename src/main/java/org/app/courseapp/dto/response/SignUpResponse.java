package org.app.courseapp.dto.response;

import lombok.*;
import org.app.courseapp.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponse {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private String fathername;
    private Integer age;
    private LocalDateTime createdAt;
    private String message;

    public static SignUpResponse fromEntity(User user) {
        return SignUpResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .surname(user.getSurname())
                .fathername(user.getFathername())
                .age(user.getAge())
                .createdAt(user.getCreatedDate())
                .message("User registered successfully")
                .build();
    }
}

