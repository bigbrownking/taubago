package org.app.courseapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.app.courseapp.model.users.Specialist;

import java.time.LocalDate;

@Entity
@Table(name = "specialist_certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialistCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_id", nullable = false)
    private Specialist specialist;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "is_verified")
    private boolean verified;

    @Column(name = "issued_at")
    private LocalDate issuedAt;
}