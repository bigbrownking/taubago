package org.app.courseapp.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.model.User;
import org.app.courseapp.model.UserRole;
import org.app.courseapp.repository.UserRepository;
import org.app.courseapp.repository.UserRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("🚀 Starting data initialization...");

        // Создаём роли
        UserRole roleUser = createRoleIfNotExists("ROLE_USER");
        UserRole roleAdmin = createRoleIfNotExists("ROLE_ADMIN");

        // Создаём админа по умолчанию
        createAdminIfNotExists(roleAdmin);

        log.info("✅ Data initialization completed");
    }

    private UserRole createRoleIfNotExists(String roleName) {
        return userRoleRepository.findByName(roleName)
                .orElseGet(() -> {
                    UserRole role = new UserRole();
                    role.setName(roleName);
                    UserRole saved = userRoleRepository.save(role);
                    log.info("✅ Created role: {}", roleName);
                    return saved;
                });
    }

    private void createAdminIfNotExists(UserRole adminRole) {
        String adminEmail = "admin@gmail.com";

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .email(adminEmail)
                    .name("Admin")
                    .surname("System")
                    .password(passwordEncoder.encode("Admin123!"))
                    .active(true)
                    .roles(new HashSet<>(Set.of(adminRole)))
                    .build();

            userRepository.save(admin);
            log.info("✅ Created admin user: {}", adminEmail);
            log.warn("⚠️  Default admin credentials:");
            log.warn("    Email: {}", adminEmail);
            log.warn("    Password: Admin123!");
            log.warn("    CHANGE PASSWORD IMMEDIATELY!");
        } else {
            log.debug("ℹ️  Admin user already exists");
        }
    }
}