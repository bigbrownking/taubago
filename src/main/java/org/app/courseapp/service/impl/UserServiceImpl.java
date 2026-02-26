package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SpecializationRepository specializationRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;

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
    public BaseUserProfileDto updateMyProfile(UpdateProfileRequest request) {
        User currentUser = getCurrentUser();

        if (currentUser instanceof Parent parent) {
            parent.setName(request.getName());
            parent.setSurname(request.getSurname());
            parent.setPhoneNumber(request.getPhoneNumber());

            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                parent.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            userRepository.save(parent);
            log.info("Parent profile updated: {}", parent.getEmail());

        } else if (currentUser instanceof Administrator admin) {
            admin.setName(request.getName());
            admin.setSurname(request.getSurname());
            admin.setPhoneNumber(request.getPhoneNumber());

            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                admin.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            userRepository.save(admin);
            log.info("Administrator profile updated: {}", admin.getEmail());

        } else if (currentUser instanceof Specialist specialist) {
            if (request.getSpecializations() != null || !request.getSpecializations().isEmpty()) {
                List<Specialization> specializations = specializationRepository.findAllById(request.getSpecializations());
                specialist.setSpecializations(specializations);
            }

            specialist.setName(request.getName());
            specialist.setSurname(request.getSurname());
            specialist.setPhoneNumber(request.getPhoneNumber());

            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                specialist.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            userRepository.save(specialist);
            log.info("Specialist profile updated: {}", specialist.getEmail());
        }

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
}