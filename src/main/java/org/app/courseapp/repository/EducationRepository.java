package org.app.courseapp.repository;

import org.app.courseapp.model.SpecialistEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationRepository extends JpaRepository<SpecialistEducation, Long> {
}
