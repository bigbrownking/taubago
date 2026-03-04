package org.app.courseapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SpecialistDetailDto {
    private Long id;
    private String name;
    private String surname;
    private String profilePictureUrl;
    private String phoneNumber;
    private String profession;
    private Integer sessionCount;
    private Double rating;
    private Integer experienceYears;
    private boolean hasFreeSession;
    private Integer pricePerHour;
    private List<String> specializations;
    private String telegramUrl;
    private String about;
    private List<EducationDto> educations;
    private List<WorkExperienceDto> workExperiences;
    private List<CertificateDto> certificates;
}
