package org.app.courseapp.dto.response;

import lombok.*;
import org.app.courseapp.model.users.Parent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private String phoneNumber;
    private LocalDateTime createdAt;
    private String message;
    private List<ChildInfo> children;
    private Integer totalAnswers;
    private Integer positiveAnswers;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChildInfo {
        private Long id;
        private String name;
        private String surname;
        private Integer age;
        private String diagnosis;
    }

    public static SignUpResponse fromEntity(Parent parent, Integer totalAnswers, Integer positiveAnswers) {
        return SignUpResponse.builder()
                .id(parent.getId())
                .email(parent.getEmail())
                .name(parent.getName())
                .surname(parent.getSurname())
                .phoneNumber(parent.getPhoneNumber())
                .createdAt(parent.getCreatedDate())
                .children(parent.getChildren().stream()
                        .map(child -> ChildInfo.builder()
                                .id(child.getId())
                                .name(child.getName())
                                .surname(child.getSurname())
                                .age(child.getAge())
                                .diagnosis(child.getDiagnosis().getName())
                                .build())
                        .collect(Collectors.toList()))
                .totalAnswers(totalAnswers)
                .positiveAnswers(positiveAnswers)
                .message("Parent registered successfully")
                .build();
    }
}