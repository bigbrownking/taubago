package org.app.courseapp.model.users;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "specialists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Specialist extends User {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "experience_years")
    private int experienceYears;

    @ElementCollection
    @CollectionTable(name = "specialist_focuses", joinColumns = @JoinColumn(name = "specialist_id"))
    @Column(name = "focus")
    private List<String> focuses = new ArrayList<>();

    @Column(name = "phone_number")
    private String phoneNumber;
}
