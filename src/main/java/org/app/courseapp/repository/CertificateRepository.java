package org.app.courseapp.repository;

import org.app.courseapp.model.SpecialistCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CertificateRepository extends JpaRepository<SpecialistCertificate, Long> {
}
