package org.app.courseapp.repository;

import org.app.courseapp.model.GroupSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupSessionRepository extends JpaRepository<GroupSession, Long> {
    List<GroupSession> findBySpecialistIdAndActiveTrue(Long specialistId);
    List<GroupSession> findAllByActiveTrue();
}
