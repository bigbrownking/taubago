package org.app.courseapp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.dto.request.*;
import org.app.courseapp.dto.response.JwtResponse;
import org.app.courseapp.dto.response.PasswordResetResponse;
import org.app.courseapp.dto.response.SignUpResponse;
import org.app.courseapp.model.PasswordResetToken;
import org.app.courseapp.model.RegistrationAnswer;
import org.app.courseapp.model.RegistrationQuestion;
import org.app.courseapp.model.users.Child;
import org.app.courseapp.model.users.Parent;
import org.app.courseapp.model.users.User;
import org.app.courseapp.model.UserRole;
import org.app.courseapp.repository.*;
import org.app.courseapp.security.jwt.JwtTokenUtil;
import org.app.courseapp.service.AuthService;
import org.app.courseapp.service.EmailService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final ParentRepository parentRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RegistrationQuestionRepository registrationQuestionRepository;
    private final RegistrationAnswerRepository registrationAnswerRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;


    @Override
    @Transactional
    public SignUpResponse signup(SignUpRequest request) {
        log.info("Creating new parent: {} {}", request.getParentName(), request.getParentSurname());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Email already exists: {}", request.getEmail());
            throw new RuntimeException("Email is already registered");
        }

        // Проверяем, что все вопросы существуют
        List<Long> questionIds = request.getRegistrationAnswers().stream()
                .map(SignUpRequest.QuestionAnswer::getQuestionId)
                .toList();

        List<RegistrationQuestion> questions = registrationQuestionRepository.findAllById(questionIds);
        if (questions.size() != questionIds.size()) {
            throw new RuntimeException("Some questions not found");
        }

        UserRole parentRole = userRoleRepository.findByName("ROLE_PARENT")
                .orElseThrow(() -> new RuntimeException("Parent role not found"));

        // Создаем родителя
        Parent parent = Parent.builder()
                .email(request.getEmail())
                .name(request.getParentName())
                .surname(request.getParentSurname())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .pincode(request.getPinCode() != null ?
                        passwordEncoder.encode(request.getPinCode()) : null)
                .active(true)
                .roles(new HashSet<>(){{add(parentRole);}})
                .children(new HashSet<>())
                .build();

        // Добавляем первого ребенка, если указан
        if (request.getFirstChild() != null) {
            Child child = Child.builder()
                    .name(request.getFirstChild().getName())
                    .surname(request.getFirstChild().getSurname())
                    .age(request.getFirstChild().getAge())
                    .diagnosis(request.getFirstChild().getDiagnosis())
                    .active(true)
                    .build();
            parent.addChild(child);
        }

        Parent savedParent = parentRepository.save(parent);

        // Сохраняем ответы на вопросы
        Map<Long, RegistrationQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(RegistrationQuestion::getId, q -> q));

        List<RegistrationAnswer> answers = request.getRegistrationAnswers().stream()
                .map(qa -> RegistrationAnswer.builder()
                        .parent(savedParent)
                        .question(questionMap.get(qa.getQuestionId()))
                        .answer(qa.getAnswer())
                        .build())
                .toList();

        registrationAnswerRepository.saveAll(answers);

        // Подсчитываем статистику ответов
        long positiveAnswers = answers.stream().filter(RegistrationAnswer::getAnswer).count();

        log.info("Parent created successfully with ID: {}. Answers: {}/{} positive",
                savedParent.getId(), positiveAnswers, answers.size());

        return SignUpResponse.fromEntity(savedParent, answers.size(), (int) positiveAnswers);
    }
    @Override
    public JwtResponse login(LoginRequest request) {
        log.info("User attempts to login: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean authenticated = false;
        String authType = null;

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                authenticated = true;
                authType = "PASSWORD";
            }
        }

        if (!authenticated && request.getPincode() != null && !request.getPincode().isBlank()) {
            if (user.getPincode() != null &&
                    passwordEncoder.matches(request.getPincode(), user.getPincode())) {
                authenticated = true;
                authType = "PIN";
            }
        }

        if (!authenticated) {
            throw new RuntimeException("Invalid password or pin code");
        }

        String token = jwtTokenUtil.generateTokenFromUsername(user.getEmail(), authType);
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getEmail());

        return JwtResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .type("Bearer")
                .username(user.getEmail())
                .authType(authType)
                .build();
    }

    @Override
    public JwtResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        log.info("Attempting to refresh token");

        if (!jwtTokenUtil.validateRefreshToken(refreshToken)) {
            log.error("Invalid refresh token");
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtTokenUtil.getUsernameFromJwtToken(refreshToken);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String authType = user.getPincode() != null ? "PIN" : "PASSWORD";

        String newAccessToken = jwtTokenUtil.generateTokenFromUsername(user.getEmail(), authType);
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(user.getEmail());

        log.info("Token refreshed successfully for user: {}", username);

        return JwtResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .type("Bearer")
                .username(user.getEmail())
                .authType(authType)
                .build();
    }

    @Override
    @Transactional
    public PasswordResetResponse requestPasswordReset(PasswordResetRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        passwordResetTokenRepository.deleteByUserId(user.getId());

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Получаем имя пользователя в зависимости от типа
        String userName = getUserName(user);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), userName, token);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage());
            throw new RuntimeException("Failed to send password reset email");
        }

        return PasswordResetResponse.builder()
                .message("Password reset email sent successfully. Please check your email.")
                .build();
    }

    @Override
    @Transactional
    public PasswordResetResponse confirmPasswordReset(PasswordResetConfirmRequest request) {
        log.info("Confirming password reset with token");

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        if (resetToken.isUsed()) {
            throw new RuntimeException("Reset token has already been used");
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password successfully reset for user: {}", user.getEmail());

        String userName = getUserName(user);

        try {
            emailService.sendEmail(
                    user.getEmail(),
                    "DiaBalance - Password Changed Successfully",
                    String.format("""
                            Hi %s,

                            Your password has been successfully changed.

                            If you didn't make this change, please contact support immediately.

                            Best regards,
                            DiaBalance Team""", userName)
            );
        } catch (Exception e) {
            log.error("Failed to send password change confirmation email: {}", e.getMessage());
        }

        return PasswordResetResponse.builder()
                .message("Password has been reset successfully. You can now login with your new password.")
                .build();
    }

    @Override
    public boolean validateResetToken(String token) {
        log.debug("Validating reset token");

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndUsedFalse(token)
                .orElse(null);

        if (resetToken == null) {
            return false;
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        return !resetToken.isUsed();
    }

    /**
     * Вспомогательный метод для получения имени пользователя в зависимости от его типа
     */
    private String getUserName(User user) {
        if (user instanceof Parent) {
            return ((Parent) user).getName();
        }
        // Можно добавить другие типы
        return user.getEmail(); // fallback
    }
}