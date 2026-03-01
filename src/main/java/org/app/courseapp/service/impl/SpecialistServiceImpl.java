package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.app.courseapp.dto.response.SpecialistCardDto;
import org.app.courseapp.dto.response.SpecialistDetailDto;
import org.app.courseapp.model.Specialization;
import org.app.courseapp.model.users.Specialist;
import org.app.courseapp.repository.SpecialistRepository;
import org.app.courseapp.service.SpecialistService;
import org.app.courseapp.util.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecialistServiceImpl implements SpecialistService {

    private final SpecialistRepository specialistRepository;
    private final Mapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<SpecialistCardDto> getAllSpecialists(Long specializationId) {
        return specialistRepository.findAllBySpecialization(specializationId)
                .stream()
                .map(mapper::convertToSpecialistCardDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SpecialistDetailDto getSpecialistById(Long id) {
        Specialist s = specialistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specialist not found: " + id));

        return mapper.convertToSpecialistDetailDto(s);
    }
}