package org.app.courseapp.repository;

import org.app.courseapp.model.RegistrationQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationQuestionRepository extends JpaRepository<RegistrationQuestion, Long> {
    List<RegistrationQuestion> findAllByIsActiveTrueOrderByOrderNumber();
}