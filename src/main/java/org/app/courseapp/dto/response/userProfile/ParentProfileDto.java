package org.app.courseapp.dto.response.userProfile;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentProfileDto extends BaseUserProfileDto {
    private String name;
    private String surname;
    private String phoneNumber;
    private String profilePictureUrl;
    private List<ChildDto> children;
    private Integer totalChildren;
    private RegistrationStats registrationStats;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrationStats {
        private Integer totalQuestions;
        private Integer positiveAnswers;
        private Integer negativeAnswers;
        private Double positivePercentage;
    }
}