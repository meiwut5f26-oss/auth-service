package service.CSFC.CSFC_auth_service.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import service.CSFC.CSFC_auth_service.model.dto.request.RolesRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.ServiceRbpRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.RolesResponse;
import service.CSFC.CSFC_auth_service.service.AdminPermissionsService;
import service.CSFC.CSFC_auth_service.service.RolesService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RbpControllerTest {

    @Mock
    RolesService rolesService;

    @Mock
    AdminPermissionsService adminPermissionsService;

    @InjectMocks
    RbpController controller;

    @Test
    void register_ShouldCreateRolesAndAssignPermissions() {
        ServiceRbpRequest.RoleRbp roleDto = new ServiceRbpRequest.RoleRbp("ADMIN", List.of("USER_READ", "USER_WRITE"));
        ServiceRbpRequest request = new ServiceRbpRequest("auth-service", List.of(roleDto));

        when(rolesService.getAllRoles()).thenReturn(List.of());
        when(rolesService.createRole(any(RolesRequest.class))).thenReturn(new RolesResponse(1, "ADMIN", null));

        ResponseEntity<String> response = controller.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Registered");
        verify(adminPermissionsService).addPermissionToRole(eq(1), eq("USER_READ"));
        verify(adminPermissionsService).addPermissionToRole(eq(1), eq("USER_WRITE"));
    }
}
