package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.app.courseapp.model.Diagnosis;
import org.app.courseapp.repository.DiagnosisRepository;
import org.app.courseapp.service.DiagnosisService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DiagnosisServiceImpl implements DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Diagnosis> getAll() {
        return diagnosisRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Diagnosis getById(Long id) {
        return diagnosisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diagnosis not found"));
    }

    @Override
    public Diagnosis create(String name) {
        if (diagnosisRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Diagnosis already exists: " + name);
        }
        return diagnosisRepository.save(Diagnosis.builder().name(name).build());
    }

    @Override
    public void delete(Long id) {
        diagnosisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diagnosis not found"));
        diagnosisRepository.deleteById(id);
    }
}
