package org.app.courseapp.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.model.RegistrationQuestion;
import org.app.courseapp.model.Video;
import org.app.courseapp.model.VideoCategory;
import org.app.courseapp.model.users.Administrator;
import org.app.courseapp.model.users.User;
import org.app.courseapp.model.UserRole;
import org.app.courseapp.repository.RegistrationQuestionRepository;
import org.app.courseapp.repository.UserRepository;
import org.app.courseapp.repository.UserRoleRepository;
import org.app.courseapp.repository.VideoCategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRoleRepository userRoleRepository;
    private final VideoCategoryRepository categoryRepository;
    private final RegistrationQuestionRepository registrationQuestionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("🚀 Starting data initialization...");

        // Создаём роли
        UserRole roleParent = createRoleIfNotExists("ROLE_PARENT");
        UserRole roleAdmin = createRoleIfNotExists("ROLE_ADMIN");
        UserRole roleSpecialist = createRoleIfNotExists("ROLE_SPECIALIST");
        UserRole roleCurator = createRoleIfNotExists("ROLE_CURATOR");

        // Создаём админа по умолчанию
        createAdminIfNotExists(roleAdmin);

        createRegistrationQuestions();
        createVideoCategories();
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
            Administrator admin = Administrator.builder()
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

    private void createRegistrationQuestions(){
        if (registrationQuestionRepository.count() > 0) {
            log.debug("ℹ️  Registration questions already exist");
            return;
        }
        int order = 1;
        registrationQuestionRepository.save(RegistrationQuestion.builder().question("Была ли гипоксия (кислородное голодание) при родах?").topic("Роды и беременность").orderNumber(order++).isActive(true).build());
        registrationQuestionRepository.save(RegistrationQuestion.builder().question("Были ли родовые травмы или инфекции в первый месяц?").topic("Роды и беременность").orderNumber(order++).isActive(true).build());
        registrationQuestionRepository.save(RegistrationQuestion.builder().question("Протекала ли беременность с серьезными осложнениями?").topic("Роды и беременность").orderNumber(order++).isActive(true).build());
        registrationQuestionRepository.save(RegistrationQuestion.builder().question("Ребенок уверенно держит голову и переворачивается?").topic("Моторика").orderNumber(order++).isActive(true).build());
        registrationQuestionRepository.save((RegistrationQuestion.builder().question("Умеет ли ребенок самостоятельно сидеть и ползать?")).topic("Моторика").orderNumber(order++).isActive(true).build());
        registrationQuestionRepository.save(RegistrationQuestion.builder().question("Может ли ребенок захватывать мелкие предметы пальцами?").topic("Моторика").orderNumber(order++).isActive(true).build());
        registrationQuestionRepository.save(RegistrationQuestion.builder().question("Реагирует ли ребенок на свое имя?").topic("Речь").orderNumber(order++).isActive(true).build());
        registrationQuestionRepository.save(RegistrationQuestion.builder().question("Понимает ли ребенок простые инструкции (дай, принеси)?").topic("Речь").orderNumber(order++).isActive(true).build());
        registrationQuestionRepository.save(RegistrationQuestion.builder().question("Есть ли в лексиконе указательные жесты или первые слова?").topic("Речь").orderNumber(order++).isActive(true).build());
        registrationQuestionRepository.save(RegistrationQuestion.builder().question("Устанавливает ли ребенок зрительный контакт?").topic("Социализация").orderNumber(order++).isActive(true).build());
        registrationQuestionRepository.save(RegistrationQuestion.builder().question("Проявляет ли ребенок интерес к играм с другими детьми?").topic("Социализация").orderNumber(order++).isActive(true).build());
        registrationQuestionRepository.save(RegistrationQuestion.builder().question("Есть ли у ребенка комплекс оживления (улыбка в ответ)?").topic("Социализация").orderNumber(order++).isActive(true).build());
        registrationQuestionRepository.save(RegistrationQuestion.builder().question("Проявляет ли ребенок интерес к самостоятельному приему пищи?").topic("Самообслуживание").orderNumber(order++).isActive(true).build());
        registrationQuestionRepository.save(RegistrationQuestion.builder().question("Дает ли ребенок знать о физиологических нуждах?").topic("Самообслуживание").orderNumber(order++).isActive(true).build());
        registrationQuestionRepository.save(RegistrationQuestion.builder().question("Пытается ли ребенок помогать при одевании?").topic("Самообслуживание").orderNumber(order++).isActive(true).build());
    }
    private void createVideoCategories(){
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                    VideoCategory.builder().name("Крупная моторика").build(),
                    VideoCategory.builder().name("Мелкая моторика").build(),
                    VideoCategory.builder().name("Зрительно-слуховое восприятие").build()
            ));
        }
    }
}