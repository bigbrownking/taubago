package org.app.courseapp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.response.RegistrationQuestionDto;
import org.app.courseapp.repository.RegistrationQuestionRepository;
import org.app.courseapp.util.Mapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dict")
@Tag(name = "Dictionary Management", description = "APIs for dict management operations")
public class DictionaryController {
    private final Mapper mapper;
    private final RegistrationQuestionRepository registrationQuestionRepository;

    @GetMapping("/questions")
    public List<RegistrationQuestionDto> getRegistrationQuestions(){
        return mapper.convertRegistrationQuestionsToDto(
                registrationQuestionRepository.findAllByIsActiveTrueOrderByOrderNumber()
        );
    }

}
