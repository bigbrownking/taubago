package org.app.courseapp.dto.response.userProfile;

import lombok.*;
import org.app.courseapp.dto.response.CertificateDto;
import org.app.courseapp.dto.response.EducationDto;
import org.app.courseapp.dto.response.WorkExperienceDto;
import org.app.courseapp.model.Specialization;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpecialistProfileDto extends BaseUserProfileDto {
    private String name;
    private String surname;
    private List<Specialization> specialization;
    private String phoneNumber;
    private String profilePictureUrl;
    private Integer experienceYears;
    private String telegramUrl;
    private boolean hasFreeSession;
    private Integer pricePerHour;
    private Double rating;
    private String about;
    private String profession;
    private int sessionCount;
    private List<EducationDto> educations;
    private List<WorkExperienceDto> workExperiences;
    private List<CertificateDto> certificates;
}