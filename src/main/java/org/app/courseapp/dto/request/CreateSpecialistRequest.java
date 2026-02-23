package org.app.courseapp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSpecialistRequest {
    private String name;
    private String surname;
    private String specialization;
    private int experienceYears;
    private List<String> focuses;
    private String phoneNumber;
    private String email;
}
