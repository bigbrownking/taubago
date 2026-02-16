package org.app.courseapp.dto.response.userProfile;

import lombok.*;
import org.app.courseapp.dto.response.userProfile.BaseUserProfileDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdministratorProfileDto extends BaseUserProfileDto {
    private String name;
    private String surname;
    private String phoneNumber;
}