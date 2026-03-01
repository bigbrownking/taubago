package org.app.courseapp.repository;

import org.app.courseapp.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByParentIdOrderByBookedAtDesc(Long parentId);
}
