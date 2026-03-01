package org.app.courseapp.service;

import org.app.courseapp.dto.response.SpecialistCardDto;
import org.app.courseapp.dto.response.SpecialistDetailDto;

import java.util.List;

public interface SpecialistService {
    List<SpecialistCardDto> getAllSpecialists(Long specializationId);
    SpecialistDetailDto getSpecialistById(Long id);
}
