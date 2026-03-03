package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import org.app.courseapp.config.minio.MinioBucket;
import org.app.courseapp.dto.request.AddCertificateRequest;
import org.app.courseapp.dto.request.AddEducationRequest;
import org.app.courseapp.dto.request.AddWorkExperienceRequest;
import org.app.courseapp.dto.request.UpdateAboutRequest;
import org.app.courseapp.dto.response.*;
import org.app.courseapp.model.SpecialistCertificate;
import org.app.courseapp.model.SpecialistEducation;
import org.app.courseapp.model.SpecialistWorkExperience;
import org.app.courseapp.model.Specialization;
import org.app.courseapp.model.users.Specialist;
import org.app.courseapp.model.users.User;
import org.app.courseapp.repository.CertificateRepository;
import org.app.courseapp.repository.EducationRepository;
import org.app.courseapp.repository.SpecialistRepository;
import org.app.courseapp.repository.WorkExperienceRepository;
import org.app.courseapp.service.SpecialistService;
import org.app.courseapp.service.UserService;
import org.app.courseapp.util.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.apache.commons.compress.utils.FileNameUtils.getExtension;

@Service
@RequiredArgsConstructor
public class SpecialistServiceImpl implements SpecialistService {

    private final SpecialistRepository specialistRepository;
    private final CertificateRepository certificateRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final EducationRepository educationRepository;
    private final UserService userService;
    private final Mapper mapper;
    private final MinioService minioService;

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
    private Specialist getCurrentSpecialist() {
        User user = userService.getCurrentUser();
        if (!(user instanceof Specialist)) {
            throw new RuntimeException("Access denied: not a specialist");
        }
        return (Specialist) user;
    }

    // --- About ---
    public void updateAbout(UpdateAboutRequest request) {
        Specialist specialist = getCurrentSpecialist();
        specialist.setAbout(request.getAbout());
        specialistRepository.save(specialist);
    }

    // --- Education ---
    public EducationDto addEducation(AddEducationRequest request) {
        Specialist specialist = getCurrentSpecialist();
        SpecialistEducation edu = SpecialistEducation.builder()
                .specialist(specialist)
                .institution(request.getInstitution())
                .degree(request.getDegree())
                .yearFrom(request.getYearFrom())
                .yearTo(request.getYearTo())
                .build();
        return mapper.convertToEducationDto(educationRepository.save(edu));
    }

    public void deleteEducation(Long educationId) {
        Specialist specialist = getCurrentSpecialist();
        SpecialistEducation edu = educationRepository.findById(educationId)
                .orElseThrow(() -> new RuntimeException("Education not found"));
        if (!edu.getSpecialist().getId().equals(specialist.getId())) {
            throw new RuntimeException("Access denied");
        }
        educationRepository.delete(edu);
    }

    public WorkExperienceDto addWorkExperience(AddWorkExperienceRequest request) {
        Specialist specialist = getCurrentSpecialist();
        SpecialistWorkExperience exp = SpecialistWorkExperience.builder()
                .specialist(specialist)
                .organization(request.getOrganization())
                .position(request.getPosition())
                .yearFrom(request.getYearFrom())
                .yearTo(request.getYearTo())
                .current(request.isCurrent())
                .build();
        return mapper.convertToWorkExperienceDto(workExperienceRepository.save(exp));
    }

    public void deleteWorkExperience(Long expId) {
        Specialist specialist = getCurrentSpecialist();
        SpecialistWorkExperience exp = workExperienceRepository.findById(expId)
                .orElseThrow(() -> new RuntimeException("Work experience not found"));
        if (!exp.getSpecialist().getId().equals(specialist.getId())) {
            throw new RuntimeException("Access denied");
        }
        workExperienceRepository.delete(exp);
    }

    // --- Certificates ---
    public CertificateDto addCertificate(AddCertificateRequest request) {
        Specialist specialist = getCurrentSpecialist();
        SpecialistCertificate cert = SpecialistCertificate.builder()
                .specialist(specialist)
                .title(request.getTitle())
                .issuedAt(request.getIssuedAt())
                .build();
        return mapper.convertToCertificateDto(certificateRepository.save(cert));
    }



    public void deleteCertificate(Long certId) {
        Specialist specialist = getCurrentSpecialist();
        SpecialistCertificate cert = certificateRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
        if (!cert.getSpecialist().getId().equals(specialist.getId())) {
            throw new RuntimeException("Access denied");
        }
        certificateRepository.delete(cert);
    }

    public CertificateDto uploadCertificateDocument(Long certId, MultipartFile file) throws IOException {
        Specialist specialist = getCurrentSpecialist();
        SpecialistCertificate cert = certificateRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
        if (!cert.getSpecialist().getId().equals(specialist.getId())) {
            throw new RuntimeException("Access denied");
        }

        String key = minioService.generateCertificateKey(specialist.getId(), certId, file.getOriginalFilename());
        minioService.uploadFile(MinioBucket.DOCUMENT, key, file.getInputStream(), file.getContentType(), file.getSize());

        cert.setDocumentUrl(key);
        return mapper.convertToCertificateDto(certificateRepository.save(cert));
    }

}