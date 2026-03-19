package service.CSFC.CSFC_auth_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.CSFC.CSFC_auth_service.model.dto.request.RolesRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.ServiceRbpRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.RolesResponse;
import service.CSFC.CSFC_auth_service.service.AdminPermissionsService;
import service.CSFC.CSFC_auth_service.service.RolesService;


@RestController
@RequestMapping("/rbp")
@RequiredArgsConstructor
@Slf4j
public class RbpController {

    private final RolesService rolesService;
    private final AdminPermissionsService permissionsService;
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody ServiceRbpRequest request) {
        log.info("RBP registration request received for service: {}", request.getServiceName());

        request.getRoles().forEach(roleDto -> {
            log.info("Processing role: {}", roleDto.getName());

            RolesResponse finalRole = getOrCreateRole(roleDto.getName());
            log.info("Role resolved - id: {}, name: {}", finalRole.getId(), finalRole.getRoleName());

            roleDto.getPermissions().forEach(perm -> {
                try {
                    permissionsService.addPermissionToRole(finalRole.getId(), perm);
                    log.info("Permission added: {} -> role: {}", perm, roleDto.getName());
                } catch (RuntimeException e) {
                    log.warn("Permission skipped (already exists?): {} -> role: {} | reason: {}", perm, roleDto.getName(), e.getMessage());
                }
            });
        });

        return ResponseEntity.ok("Registered: " + request.getServiceName());
    }

    private RolesResponse getOrCreateRole(String roleName) {
        try {
            RolesResponse role = rolesService.createRole(new RolesRequest(roleName));
            log.info("Created new role: {}", roleName);
            return role;
        } catch (RuntimeException e) {
            log.warn("Role already exists, fetching: {} | reason: {}", roleName, e.getMessage());
            return rolesService.getAllRoles().stream()
                    .filter(r -> r.getRoleName().equals(roleName))
                    .findFirst()
                    .orElseThrow();
        }
    }
}