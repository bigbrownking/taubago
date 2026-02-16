package org.app.courseapp.dto.response.userProfile;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "userType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ParentProfileDto.class, name = "PARENT"),
        @JsonSubTypes.Type(value = AdministratorProfileDto.class, name = "ADMINISTRATOR"),
        @JsonSubTypes.Type(value = SpecialistProfileDto.class, name = "SPECIALIST")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseUserProfileDto {
    private Long id;
    private String email;
    private Boolean active;
    private LocalDateTime createdDate;
    private Set<String> roles;
    private String userType;
}