package service.CSFC.CSFC_auth_service.controller;

import service.CSFC.CSFC_auth_service.common.security.CustomerUserDetails;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;
import service.CSFC.CSFC_auth_service.model.entity.Permission;
import service.CSFC.CSFC_auth_service.model.entity.Roles;
import service.CSFC.CSFC_auth_service.model.entity.Users;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

final class TestSecurityUtils {
    private TestSecurityUtils() {
    }

    static CustomerUserDetails customerPrincipal(String roleName, String... permissions) {
        Users user = new Users();
        user.setId(UUID.randomUUID());
        user.setEmail("user@csfc.local");
        user.setPassword("encoded");
        user.setStatus(CustomerStatus.ACTIVE);

        Roles role = new Roles();
        role.setName(roleName);
        if (permissions != null && permissions.length > 0) {
            Set<Permission> permissionSet = Arrays.stream(permissions)
                    .map(name -> Permission.builder().name(name).build())
                    .collect(Collectors.toSet());
            role.setPermissions(permissionSet);
        }
        user.setRole(role);
        return new CustomerUserDetails(user);
    }
}
