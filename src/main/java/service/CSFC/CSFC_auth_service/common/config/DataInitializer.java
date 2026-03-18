package service.CSFC.CSFC_auth_service.common.config;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import service.CSFC.CSFC_auth_service.model.entity.Roles;
import service.CSFC.CSFC_auth_service.model.entity.Users;
import service.CSFC.CSFC_auth_service.repository.RolesRepository;
import service.CSFC.CSFC_auth_service.repository.UsersRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Value("${app.admin-init.enabled:false}")
    private boolean enabled;

    @org.springframework.beans.factory.annotation.Value("${app.admin-init.email:}")
    private String email;

    @org.springframework.beans.factory.annotation.Value("${app.admin-init.password:}")
    private String password;

    @org.springframework.beans.factory.annotation.Value("${app.admin-init.name:System Admin}")
    private String name;

    @org.springframework.beans.factory.annotation.Value("${app.admin-init.role-name:ADMIN}")
    private String roleName;

    @Override
    @Transactional
    public void run(@Nonnull ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            log.warn("Admin init is enabled but ADMIN_INIT_EMAIL or ADMIN_INIT_PASSWORD is missing. Skip seeding.");
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (usersRepository.existsByEmail(normalizedEmail)) {
            log.info("Admin user already exists: {}", normalizedEmail);
            return;
        }

        Roles adminRole = rolesRepository.findByName(roleName)
                .orElseGet(() -> {
                    Roles newRole = new Roles();
                    newRole.setName(roleName);
                    newRole.setCreateDate(LocalDateTime.now());
                    return rolesRepository.save(newRole);
                });

        Users admin = new Users();
        admin.setName(StringUtils.hasText(name) ? name.trim() : "System Admin");
        admin.setEmail(normalizedEmail);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setCreateDate(LocalDateTime.now());
        admin.setAddress(null);
        admin.setIsFirstLogin(false);
        admin.setIsActive(true);
        admin.setRole(adminRole);

        usersRepository.save(admin);
        log.info("Seeded admin account: {}", normalizedEmail);
    }
}
