package org.app.courseapp.service;

import org.app.courseapp.dto.response.RegistrationQuestionDto;

import java.util.List;

public interface RegistrationQuestionService {
    List<RegistrationQuestionDto> getAllActiveQuestions();
    RegistrationQuestionDto getById(Long id);
    RegistrationQuestionDto create(String question, String topic, Integer orderNumber);
    RegistrationQuestionDto update(Long id, String question, String topic, Integer orderNumber);
    void deactivate(Long id);
}
