package org.app.courseapp.model.users;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
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

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "telegram_url")
    private String telegramUrl;

    @Column(name = "has_free_session")
    private boolean hasFreeSession;

    @Column(name = "price_per_hour")
    private Integer pricePerHour;

    @Column(name = "rating")
    private Double rating;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "specialist_specializations",
            joinColumns = @JoinColumn(name = "specialist_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id")
    )
    private List<Specialization> specializations = new ArrayList<>();

    @Column(name = "phone_number")
    private String phoneNumber;
}
