package org.app.courseapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.app.courseapp.model.users.Specialist;

@Entity
@Table(name = "specialist_educations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialistEducation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_id", nullable = false)
    private Specialist specialist;

    @Column(name = "institution", nullable = false)
    private String institution;

    @Column(name = "degree")
    private String degree;

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "is_verified")
    private boolean verified;

    @Column(name = "year_from")
    private Integer yearFrom;

    @Column(name = "year_to")
    private Integer yearTo;
}
