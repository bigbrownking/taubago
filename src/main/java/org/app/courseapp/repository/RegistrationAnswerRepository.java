package org.app.courseapp.repository;

import org.app.courseapp.model.RegistrationAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationAnswerRepository extends JpaRepository<RegistrationAnswer, Long> {
    List<RegistrationAnswer> findByParentId(Long parentId);
    Long countByParentIdAndAnswerTrue(Long parentId);
}