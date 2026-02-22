package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.response.RegistrationQuestionDto;
import org.app.courseapp.model.RegistrationQuestion;
import org.app.courseapp.repository.RegistrationQuestionRepository;
import org.app.courseapp.service.RegistrationQuestionService;
import org.app.courseapp.util.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationQuestionServiceImpl implements RegistrationQuestionService {

    private final RegistrationQuestionRepository registrationQuestionRepository;
    private final Mapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<RegistrationQuestionDto> getAllActiveQuestions() {
        return mapper.convertRegistrationQuestionsToDto(
                registrationQuestionRepository.findAllByIsActiveTrueOrderByOrderNumber()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationQuestionDto getById(Long id) {
        return mapper.convertRegistrationQuestionToDto(
                registrationQuestionRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Question not found"))
        );
    }

    @Override
    public RegistrationQuestionDto create(String question, String topic, Integer orderNumber) {
        RegistrationQuestion saved = registrationQuestionRepository.save(
                RegistrationQuestion.builder()
                        .question(question)
                        .topic(topic)
                        .orderNumber(orderNumber)
                        .isActive(true)
                        .build()
        );
        return mapper.convertRegistrationQuestionToDto(saved);
    }

    @Override
    public RegistrationQuestionDto update(Long id, String question, String topic, Integer orderNumber) {
        RegistrationQuestion existing = registrationQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        existing.setQuestion(question);
        existing.setTopic(topic);
        existing.setOrderNumber(orderNumber);

        return mapper.convertRegistrationQuestionToDto(registrationQuestionRepository.save(existing));
    }

    @Override
    public void deactivate(Long id) {
        RegistrationQuestion existing = registrationQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        existing.setIsActive(false);
        registrationQuestionRepository.save(existing);
    }
}
