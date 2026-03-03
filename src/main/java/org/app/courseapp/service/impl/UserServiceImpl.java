package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.config.minio.MinioBucket;
import org.app.courseapp.dto.request.ChangePasswordRequest;
import org.app.courseapp.dto.request.RegisterCuratorRequest;
import org.app.courseapp.dto.request.RegisterSpecialistRequest;
import org.app.courseapp.dto.request.UpdateProfileRequest;
import org.app.courseapp.dto.response.*;
import org.app.courseapp.dto.response.userProfile.*;
import org.app.courseapp.model.RegistrationAnswer;
import org.app.courseapp.model.Specialization;
import org.app.courseapp.model.UserRole;
import org.app.courseapp.model.users.*;
import org.app.courseapp.repository.*;
import org.app.courseapp.service.UserService;
import org.app.courseapp.util.Mapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final SpecializationRepository specializationRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final MinioService minioService;

    @Override
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public BaseUserProfileDto getMyProfile() {
        User currentUser = getCurrentUser();
        return mapper.convertToProfileDto(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseUserProfileDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapper.convertToProfileDto(user);
    }

    @Override
    @Transactional
    public BaseUserProfileDto updateMyProfile(UpdateProfileRequest request, MultipartFile photo) throws IOException {
        User currentUser = getCurrentUser();

        if (photo != null && !photo.isEmpty()) {
            if (currentUser.getProfilePictureUrl() != null) {
                minioService.deleteFile(MinioBucket.AVATAR, currentUser.getProfilePictureUrl());
            }
            String key = minioService.generateAvatarKey(currentUser.getId(), photo.getOriginalFilename());
            minioService.uploadFile(MinioBucket.AVATAR, key, photo.getInputStream(), photo.getContentType(), photo.getSize());
            currentUser.setProfilePictureUrl(key);
        }

        currentUser.setName(request.getName());
        currentUser.setSurname(request.getSurname());
        currentUser.setPhoneNumber(request.getPhoneNumber());
        userRepository.save(currentUser);


        return mapper.convertToProfileDto(currentUser);
    }

    @Override
    @Transactional
    public void deactivateMyAccount() {
        User currentUser = getCurrentUser();
        currentUser.setActive(false);
        currentUser.setDeleted(true);
        currentUser.setDeletedAt(LocalDateTime.now());
        userRepository.save(currentUser);
        log.info("User account deleted: {}", currentUser.getId());
    }

    @Override
    @Transactional
    public BaseUserProfileDto registerCurator(RegisterCuratorRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use: " + request.getEmail());
        }

        Curator curator = Curator.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .roles(Set.of(userRoleRepository.findByName("ROLE_CURATOR")
                        .orElseThrow(() -> new RuntimeException("Default role not found"))))
                .build();

        userRepository.save(curator);
        log.info("Curator registered: {}", request.getEmail());
        return getUserProfile(curator.getEmail());
    }

    @Override
    @Transactional
    public BaseUserProfileDto registerSpecialist(RegisterSpecialistRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use: " + request.getEmail());
        }

        List<Specialization> specializations = new ArrayList<>();
        if (request.getSpecializationIds() != null && !request.getSpecializationIds().isEmpty()) {
            specializations = specializationRepository.findAllById(request.getSpecializationIds());
        }

        Specialist specialist = Specialist.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .experienceYears(request.getExperienceYears())
                .telegramUrl(request.getTelegramUrl())
                .hasFreeSession(request.isHasFreeSession())
                .pricePerHour(request.getPricePerHour())
                .rating(request.getRating())
                .specializations(specializations)
                .sessionCount(0)
                .roles(Set.of(userRoleRepository.findByName("ROLE_SPECIALIST")
                        .orElseThrow(() -> new RuntimeException("Default role not found"))))
                .build();

        userRepository.save(specialist);
        log.info("Specialist registered: {}", request.getEmail());
        return getUserProfile(specialist.getEmail());
    }

    @Override
    public void changeMyPassword(ChangePasswordRequest request) {
        User currentUser = getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
        log.info("User {} changed password", currentUser.getEmail());
    }
}