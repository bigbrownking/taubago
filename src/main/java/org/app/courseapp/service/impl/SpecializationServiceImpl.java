package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.app.courseapp.model.Specialization;
import org.app.courseapp.repository.SpecializationRepository;
import org.app.courseapp.service.SpecializationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecializationServiceImpl implements SpecializationService {
    private final SpecializationRepository specializationRepository;
    @Override
    @Transactional(readOnly = true)
    public List<Specialization> getAll() {
        return specializationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Specialization getById(Long id) {
        return specializationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specialization not found"));
    }

    @Override
    public Specialization create(String name) {
        if (specializationRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Specialization already exists: " + name);
        }
        return specializationRepository.save(Specialization.builder().name(name).build());
    }

    @Override
    public void delete(Long id) {
        specializationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specialization not found"));
        specializationRepository.deleteById(id);
    }
}
