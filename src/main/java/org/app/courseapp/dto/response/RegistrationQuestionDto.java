package org.app.courseapp.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationQuestionDto {
    private Long id;
    private String question;
    private String topic;
}