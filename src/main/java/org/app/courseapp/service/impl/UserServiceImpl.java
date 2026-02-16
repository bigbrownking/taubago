package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.dto.request.UpdateProfileRequest;
import org.app.courseapp.dto.response.*;
import org.app.courseapp.dto.response.userProfile.*;
import org.app.courseapp.model.RegistrationAnswer;
import org.app.courseapp.model.UserRole;
import org.app.courseapp.model.users.*;
import org.app.courseapp.repository.RegistrationAnswerRepository;
import org.app.courseapp.repository.UserRepository;
import org.app.courseapp.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RegistrationAnswerRepository registrationAnswerRepository;
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
        return convertToProfileDto(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseUserProfileDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToProfileDto(user);
    }

    @Override
    @Transactional
    public BaseUserProfileDto updateMyProfile(UpdateProfileRequest request) {
        User currentUser = getCurrentUser();

        if (currentUser instanceof Parent) {
            Parent parent = (Parent) currentUser;
            parent.setName(request.getName());
            parent.setSurname(request.getSurname());
            parent.setPhoneNumber(request.getPhoneNumber());

            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                parent.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            userRepository.save(parent);
            log.info("Parent profile updated: {}", parent.getEmail());

        } else if (currentUser instanceof Administrator) {
            Administrator admin = (Administrator) currentUser;
            admin.setName(request.getName());
            admin.setSurname(request.getSurname());
            admin.setPhoneNumber(request.getPhoneNumber());

            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                admin.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            userRepository.save(admin);
            log.info("Administrator profile updated: {}", admin.getEmail());

        } else if (currentUser instanceof Specialist) {
            Specialist specialist = (Specialist) currentUser;
            specialist.setName(request.getName());
            specialist.setSurname(request.getSurname());
            specialist.setPhoneNumber(request.getPhoneNumber());
            specialist.setSpecialization(request.getSpecialization());

            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                specialist.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            userRepository.save(specialist);
            log.info("Specialist profile updated: {}", specialist.getEmail());
        }

        return convertToProfileDto(currentUser);
    }

    // Helper method to convert User to appropriate DTO
    private BaseUserProfileDto convertToProfileDto(User user) {
        if (user instanceof Parent) {
            return convertParentToDto((Parent) user);
        } else if (user instanceof Administrator) {
            return convertAdministratorToDto((Administrator) user);
        } else if (user instanceof Specialist) {
            return convertSpecialistToDto((Specialist) user);
        }
        throw new RuntimeException("Unknown user type");
    }

    private ParentProfileDto convertParentToDto(Parent parent) {
        ParentProfileDto dto = new ParentProfileDto();
        dto.setId(parent.getId());
        dto.setEmail(parent.getEmail());
        dto.setActive(parent.isActive());
        dto.setCreatedDate(parent.getCreatedDate());
        dto.setRoles(parent.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()));
        dto.setUserType("PARENT");
        dto.setName(parent.getName());
        dto.setSurname(parent.getSurname());
        dto.setPhoneNumber(parent.getPhoneNumber());
        dto.setProfilePictureUrl(parent.getProfilePictureUrl());

        // Children
        List<ChildDto> children = parent.getChildren().stream()
                .filter(Child::isActive)
                .map(ChildDto::fromEntity)
                .collect(Collectors.toList());
        dto.setChildren(children);
        dto.setTotalChildren(children.size());

        // Registration stats
        List<RegistrationAnswer> answers = registrationAnswerRepository.findByParentId(parent.getId());
        if (!answers.isEmpty()) {
            long positiveCount = answers.stream().filter(RegistrationAnswer::getAnswer).count();
            long negativeCount = answers.size() - positiveCount;
            double percentage = (positiveCount * 100.0) / answers.size();

            ParentProfileDto.RegistrationStats stats = ParentProfileDto.RegistrationStats.builder()
                    .totalQuestions(answers.size())
                    .positiveAnswers((int) positiveCount)
                    .negativeAnswers((int) negativeCount)
                    .positivePercentage(Math.round(percentage * 10.0) / 10.0)
                    .build();
            dto.setRegistrationStats(stats);
        }

        return dto;
    }

    private AdministratorProfileDto convertAdministratorToDto(Administrator admin) {
        AdministratorProfileDto dto = new AdministratorProfileDto();
        dto.setId(admin.getId());
        dto.setEmail(admin.getEmail());
        dto.setActive(admin.isActive());
        dto.setCreatedDate(admin.getCreatedDate());
        dto.setRoles(admin.getRoles().stream()
                .map(UserRole::getName)
                .collect(Collectors.toSet()));
        dto.setUserType("ADMINISTRATOR");
        dto.setName(admin.getName());
        dto.setSurname(admin.getSurname());
        dto.setPhoneNumber(admin.getPhoneNumber());
        return dto;
    }

    private SpecialistProfileDto convertSpecialistToDto(Specialist specialist) {
        SpecialistProfileDto dto = new SpecialistProfileDto();
        dto.setId(specialist.getId());
        dto.setEmail(specialist.getEmail());
        dto.setActive(specialist.isActive());
        dto.setCreatedDate(specialist.getCreatedDate());
        dto.setRoles(specialist.getRoles().stream()
                .map(UserRole::getName)
                .collect(Collectors.toSet()));
        dto.setUserType("SPECIALIST");
        dto.setName(specialist.getName());
        dto.setSurname(specialist.getSurname());
        dto.setSpecialization(specialist.getSpecialization());
        dto.setPhoneNumber(specialist.getPhoneNumber());
        return dto;
    }
}