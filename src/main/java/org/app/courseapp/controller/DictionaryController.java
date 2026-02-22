package org.app.courseapp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.response.RegistrationQuestionDto;
import org.app.courseapp.model.Diagnosis;
import org.app.courseapp.model.VideoCategory;
import org.app.courseapp.repository.RegistrationQuestionRepository;
import org.app.courseapp.service.DiagnosisService;
import org.app.courseapp.service.RegistrationQuestionService;
import org.app.courseapp.service.VideoCategoryService;
import org.app.courseapp.util.Mapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dict")
@Tag(name = "Dictionary Management", description = "APIs for dict management operations")
public class DictionaryController {
    private final VideoCategoryService videoCategoryService;
    private final RegistrationQuestionService registrationQuestionService;
    private final DiagnosisService diagnosisService;

    @GetMapping("/questions")
    public ResponseEntity<List<RegistrationQuestionDto>> getRegistrationQuestions() {
        return ResponseEntity.ok(registrationQuestionService.getAllActiveQuestions());
    }
    @GetMapping("/categories")
    public ResponseEntity<List<VideoCategory>> getCategories() {
        return ResponseEntity.ok(videoCategoryService.getAll());
    }

    @GetMapping("/diagnosis")
    public ResponseEntity<List<Diagnosis>> getDiagnosis() {
        return ResponseEntity.ok(diagnosisService.getAll());
    }



}
