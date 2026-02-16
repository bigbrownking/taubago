package org.app.courseapp.dto.response.userProfile;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpecialistProfileDto extends BaseUserProfileDto {
    private String name;
    private String surname;
    private String specialization;
    private String phoneNumber;
    private String profilePictureUrl;
}