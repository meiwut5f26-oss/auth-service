package service.CSFC.CSFC_auth_service.common.config;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;
import service.CSFC.CSFC_auth_service.model.entity.Permission;
import service.CSFC.CSFC_auth_service.model.entity.Roles;
import service.CSFC.CSFC_auth_service.model.entity.Users;
import service.CSFC.CSFC_auth_service.repository.PermissionsRepository;
import service.CSFC.CSFC_auth_service.repository.RolesRepository;
import service.CSFC.CSFC_auth_service.repository.UsersRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PermissionsRepository permissionsRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin-init.enabled:false}")
    private boolean enabled;

    @Value("${app.admin-init.email:}")
    private String email;

    @Value("${app.admin-init.password:}")
    private String password;

    @Value("${app.admin-init.name:System Admin}")
    private String name;

    @Value("${app.admin-init.role-name:ADMIN}")
    private String roleName;

    private static final List<String> BOOTSTRAP_PERMISSIONS = List.of(
            "PERMISSION_ASSIGN",
            "PERMISSION_VIEW",
            "ROLE_CREATE",
            "ROLE_UPDATE",
            "ROLE_VIEW",
            "ROLE_DELETE",
            "USER_DELETE",
            "USER_UPDATE_STATUS",
            "USER_CREATE",
            "USER_READ_SELF",
            "USER_UPDATE_ROLE",
            "CUSTOMER_PROFILE_UPDATE_SELF",
            "CUSTOMER_PROFILE_LIST",
            "CUSTOMER_PROFILE_VIEW",
            "CUSTOMER_PROFILE_UPDATE",
            "CUSTOMER_PROFILE_STATUS_UPDATE",
            "CUSTOMER_PROFILE_LOCK",
            "CUSTOMER_PROFILE_UNLOCK",
            "CUSTOMER_SEARCH",
            "CUSTOMER_ACTIVITY_VIEW",
            "INTERNAL_CUSTOMER_READ",
            "INTERNAL_CUSTOMER_WRITE",
            "CUSTOMER_AUDIT_VIEW"
    );

    private static final Map<String, List<String>> ROLE_PERMISSION_MAP = new LinkedHashMap<>() {{
        put("ADMIN", BOOTSTRAP_PERMISSIONS);
        put("STAFF", List.of("USER_READ_SELF", "CUSTOMER_PROFILE_VIEW"));
        put("CUSTOMER", List.of("USER_READ_SELF", "CUSTOMER_PROFILE_UPDATE_SELF"));
    }};

    @Override
    @Transactional
    public void run(@Nonnull ApplicationArguments args) {
        // Always seed roles first regardless of admin-init flag
        seedRoles();
        seedPermissions();
        seedRolePermissions();
        attachAllPermissionsToAdmin();

        if (!enabled) {
            log.info("Admin init is disabled. Skipping admin seeding.");
            return;
        }

        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            log.warn("Admin init is enabled but ADMIN_INIT_EMAIL or ADMIN_INIT_PASSWORD is missing. Skipping.");
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (usersRepository.existsByEmail(normalizedEmail)) {
            log.info("Admin user already exists: {}", normalizedEmail);
            return;
        }

        Roles adminRole = rolesRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found after seeding: " + roleName));

        Users admin = new Users();
        admin.setName(StringUtils.hasText(name) ? name.trim() : "System Admin");
        admin.setEmail(normalizedEmail);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setStatus(CustomerStatus.ACTIVE);
        admin.setIsFirstLogin(false);
        admin.setMarketingOptin(false);
        admin.setRole(adminRole);

        usersRepository.save(admin);
        log.info("Seeded admin account: {}", normalizedEmail);
    }

    private void seedRoles() {
        if (!rolesRepository.existsByName("ADMIN")) {
            Roles admin = new Roles();
            admin.setName("ADMIN");
            admin.setCreateDate(LocalDateTime.now());
            rolesRepository.save(admin);
            log.info("Seeded ADMIN role");
        }

        if (!rolesRepository.existsByName("CUSTOMER")) {
            Roles customer = new Roles();
            customer.setName("CUSTOMER");
            customer.setCreateDate(LocalDateTime.now());
            rolesRepository.save(customer);
            log.info("Seeded CUSTOMER role");
        }

        if (!rolesRepository.existsByName("STAFF")) {
            Roles staff = new Roles();
            staff.setName("STAFF");
            staff.setCreateDate(LocalDateTime.now());
            rolesRepository.save(staff);
            log.info("Seeded STAFF role");
        }
    }

    private void seedPermissions() {
        BOOTSTRAP_PERMISSIONS.forEach(permissionName ->
                permissionsRepository.findByName(permissionName).orElseGet(() -> {
                    Permission permission = Permission.builder()
                            .name(permissionName)
                            .description(permissionName)
                            .build();
                    log.info("Seeded permission {}", permissionName);
                    return permissionsRepository.save(permission);
                })
        );
    }

    private void seedRolePermissions() {
        ROLE_PERMISSION_MAP.forEach((roleName, permissionNames) -> {
            Roles role = rolesRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalStateException("Role not found during permission seeding: " + roleName));

            Set<Permission> permissions = new HashSet<>(role.getPermissions());
            permissionNames.forEach(permission ->
                    permissionsRepository.findByName(permission).ifPresent(permissions::add)
            );

            role.setPermissions(permissions);
            rolesRepository.save(role);
            log.info("Attached {} permissions to role {}", permissions.size(), roleName);
        });
    }

    private void attachAllPermissionsToAdmin() {
        rolesRepository.findByName("ADMIN").ifPresent(adminRole -> {
            Set<Permission> all = new HashSet<>(permissionsRepository.findAll());
            adminRole.setPermissions(all);
            rolesRepository.save(adminRole);
            log.info("Synced ADMIN role with all permissions ({} total)", all.size());
        });
    }
}