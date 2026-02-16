package org.app.courseapp.dto.response.userProfile;

import lombok.*;
import org.app.courseapp.model.users.Child;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildDto {
    private Long id;
    private String name;
    private String surname;
    private Integer age;
    private String diagnosis;
    private Boolean active;
    private LocalDateTime createdDate;

    public static ChildDto fromEntity(Child child) {
        return ChildDto.builder()
                .id(child.getId())
                .name(child.getName())
                .surname(child.getSurname())
                .age(child.getAge())
                .diagnosis(child.getDiagnosis())
                .active(child.isActive())
                .createdDate(child.getCreatedDate())
                .build();
    }
}