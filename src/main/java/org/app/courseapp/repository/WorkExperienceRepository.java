package org.app.courseapp.repository;

import org.app.courseapp.model.SpecialistWorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkExperienceRepository extends JpaRepository<SpecialistWorkExperience, Long>{
}
