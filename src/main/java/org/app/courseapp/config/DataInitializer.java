package org.app.courseapp.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.courseapp.model.*;
import org.app.courseapp.model.users.Administrator;
import org.app.courseapp.model.users.Curator;
import org.app.courseapp.model.users.Parent;
import org.app.courseapp.model.users.User;
import org.app.courseapp.repository.*;
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
    private final DiagnosisRepository diagnosisRepository;
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

        createAdminIfNotExists(roleAdmin, 1);
        createAdminIfNotExists(roleAdmin, 2);

        createParentIfNotExists(roleParent);
        createCuratorIfNotExists(roleCurator);

        createRegistrationQuestions();
        createVideoCategories();
        createDiagnosis();

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
    private void createCuratorIfNotExists(UserRole curatorRole) {
        String curatorEmail = "curator@gmail.com";

        if (!userRepository.existsByEmail(curatorEmail)) {
            Curator curator = Curator.builder()
                    .email(curatorEmail)
                    .name("Curator")
                    .surname("Curator")
                    .password(passwordEncoder.encode("Curator123!"))
                    .active(true)
                    .deleted(false)
                    .roles(new HashSet<>(Set.of(curatorRole)))
                    .build();

            userRepository.save(curator);
            log.info("✅ Created curator user: {}", curatorEmail);
            log.warn("⚠️  Default curator credentials:");
            log.warn("    Email: {}", curatorEmail);
            log.warn("    Password: Curator123!");
        } else {
            log.debug("ℹ️  Parent user already exists");
        }
    }
    private void createParentIfNotExists(UserRole parentRole) {
        String parentEmail = "parent@gmail.com";

        if (!userRepository.existsByEmail(parentEmail)) {
            Parent parent = Parent.builder()
                    .email(parentEmail)
                    .name("Parent")
                    .surname("Parent")
                    .password(passwordEncoder.encode("Parent123!"))
                    .active(true)
                    .deleted(false)
                    .roles(new HashSet<>(Set.of(parentRole)))
                    .build();

            userRepository.save(parent);
            log.info("✅ Created admin user: {}", parentEmail);
            log.warn("⚠️  Default parent credentials:");
            log.warn("    Email: {}", parentEmail);
            log.warn("    Password: Parent123!");
        } else {
            log.debug("ℹ️  Parent user already exists");
        }
    }
    private void createAdminIfNotExists(UserRole adminRole, int index) {
        String adminEmail = "admin" + index +"@gmail.com";

        if (!userRepository.existsByEmail(adminEmail)) {
            Administrator admin = Administrator.builder()
                    .email(adminEmail)
                    .name("Admin")
                    .surname("System")
                    .password(passwordEncoder.encode("Admin123!"))
                    .active(true)
                    .deleted(false)
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

    private void createDiagnosis(){
        if (diagnosisRepository.count() == 0) {
            diagnosisRepository.saveAll(List.of(
                    Diagnosis.builder().name("No diagnosis yet").build(),
                    Diagnosis.builder().name("Speech delay (ZRR)").build(),
                    Diagnosis.builder().name("Developmental delay (ZPR)").build(),
                    Diagnosis.builder().name("Autism Spectrum (ASD)").build(),
                    Diagnosis.builder().name("Other").build()
            ));
        }
    }
}