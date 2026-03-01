package org.app.courseapp.repository;

import org.app.courseapp.model.SpecialistSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SpecialistSlotRepository extends JpaRepository<SpecialistSlot, Long> {

    @Query("""
        SELECT s FROM SpecialistSlot s
        WHERE s.specialist.id = :specialistId
        AND s.date BETWEEN :from AND :to
        AND s.booked = false
        ORDER BY s.date, s.time
    """)
    List<SpecialistSlot> findAvailableSlots(
            @Param("specialistId") Long specialistId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    List<SpecialistSlot> findBySpecialistIdOrderByDateAscTimeAsc(Long specialistId);

    boolean existsBySpecialistIdAndDateAndTime(Long specialistId, LocalDate date, java.time.LocalTime time);

    @Modifying
    @Query("""
    DELETE FROM SpecialistSlot s
    WHERE s.specialist.id = :specialistId
    AND s.date >= :from
    AND s.booked = false
""")
    void deleteAllFutureUnbookedSlots(
            @Param("specialistId") Long specialistId,
            @Param("from") LocalDate from
    );
}