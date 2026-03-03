package org.app.courseapp.model.users;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.app.courseapp.model.SpecialistCertificate;
import org.app.courseapp.model.SpecialistEducation;
import org.app.courseapp.model.SpecialistWorkExperience;
import org.app.courseapp.model.Specialization;

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

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "telegram_url")
    private String telegramUrl;

    @Column(name = "has_free_session")
    private boolean hasFreeSession;

    @Column(name = "price_per_hour")
    private Integer pricePerHour;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "about", columnDefinition = "TEXT")
    private String about;

    @Column(name = "profession")
    private String profession;

    @Column(name = "session_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer sessionCount;

    @OneToMany(mappedBy = "specialist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpecialistEducation> educations = new ArrayList<>();

    @OneToMany(mappedBy = "specialist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpecialistWorkExperience> workExperiences = new ArrayList<>();

    @OneToMany(mappedBy = "specialist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpecialistCertificate> certificates = new ArrayList<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "specialist_specializations",
            joinColumns = @JoinColumn(name = "specialist_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id")
    )
    private List<Specialization> specializations = new ArrayList<>();
}
