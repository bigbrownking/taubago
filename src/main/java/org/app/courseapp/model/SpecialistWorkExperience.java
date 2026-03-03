package org.app.courseapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.app.courseapp.model.users.Specialist;

@Entity
@Table(name = "specialist_work_experiences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialistWorkExperience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_id", nullable = false)
    private Specialist specialist;

    @Column(name = "organization", nullable = false)
    private String organization;

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "year_from")
    private Integer yearFrom;

    @Column(name = "year_to")
    private Integer yearTo;

    @Column(name = "is_current")
    private boolean current;
}
