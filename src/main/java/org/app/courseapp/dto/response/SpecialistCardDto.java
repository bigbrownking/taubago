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
    private String phoneNumber;
    private double rating;
    private int experienceYears;
    private boolean hasFreeSession;
    private int pricePerHour;
    private List<String> specializations;
    private String telegramUrl;
}
