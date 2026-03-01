package org.app.courseapp.repository;

import org.app.courseapp.model.users.Specialist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecialistRepository extends JpaRepository<Specialist, Long> {

    @Query("""
        SELECT DISTINCT s FROM Specialist s
        LEFT JOIN FETCH s.specializations sp
        WHERE (:specializationId IS NULL OR sp.id = :specializationId)
        ORDER BY s.rating DESC NULLS LAST
    """)
    List<Specialist> findAllBySpecialization(@Param("specializationId") Long specializationId);
}
