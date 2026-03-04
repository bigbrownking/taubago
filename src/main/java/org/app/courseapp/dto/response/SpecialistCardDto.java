package org.app.courseapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SpecialistCardDto {
    private Long id;
    private String name;
    private String surname;
    private String profilePictureUrl;
    private String profession;
    private String phoneNumber;
    private Double rating;
    private Integer experienceYears;
    private boolean hasFreeSession;
    private Integer pricePerHour;
    private List<String> specializations;
    private String telegramUrl;
}
