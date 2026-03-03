package org.app.courseapp.service;

import org.app.courseapp.dto.request.AddCertificateRequest;
import org.app.courseapp.dto.request.AddEducationRequest;
import org.app.courseapp.dto.request.AddWorkExperienceRequest;
import org.app.courseapp.dto.request.UpdateAboutRequest;
import org.app.courseapp.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface SpecialistService {
    List<SpecialistCardDto> getAllSpecialists(Long specializationId);
    SpecialistDetailDto getSpecialistById(Long id);
    void updateAbout(UpdateAboutRequest request);
    EducationDto addEducation(AddEducationRequest request);
    void deleteEducation(Long educationId);
    WorkExperienceDto addWorkExperience(AddWorkExperienceRequest request);
    void deleteWorkExperience(Long expId);
    CertificateDto addCertificate(AddCertificateRequest request);
    CertificateDto uploadCertificateDocument(Long certId, MultipartFile file) throws IOException;
    void deleteCertificate(Long certId);
}
