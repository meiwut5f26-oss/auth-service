package service.CSFC.CSFC_auth_service.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import service.CSFC.CSFC_auth_service.common.response.BaseResponse;
import service.CSFC.CSFC_auth_service.model.dto.request.RolesRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.UpdateRolesRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.RolesResponse;
import service.CSFC.CSFC_auth_service.service.RolesService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolesControllerTest {

    @Mock
    RolesService rolesService;

    @InjectMocks
    RolesController controller;

    @Test
    void createRole_ShouldReturnRole() {
        RolesRequest request = new RolesRequest("STAFF");
        RolesResponse response = new RolesResponse(1, "STAFF", null);
        when(rolesService.createRole(any(RolesRequest.class))).thenReturn(response);

        ResponseEntity<BaseResponse<RolesResponse>> result = controller.createRole(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getRoleName()).isEqualTo("STAFF");
    }

    @Test
    void updateRole_ShouldReturnUpdatedRole() {
        UpdateRolesRequest request = new UpdateRolesRequest();
        request.setId(1);
        request.setRoleName("MANAGER");
        RolesResponse response = new RolesResponse(1, "MANAGER", null);
        when(rolesService.updateRoleById(any(UpdateRolesRequest.class))).thenReturn(response);

        ResponseEntity<BaseResponse<RolesResponse>> result = controller.updateRole(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getRoleName()).isEqualTo("MANAGER");
    }

    @Test
    void getAllRoles_ShouldReturnList() {
        when(rolesService.getAllRoles()).thenReturn(List.of(new RolesResponse(1, "STAFF", null)));

        ResponseEntity<BaseResponse> result = controller.getAllRoles();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat((List<?>) result.getBody().getData()).hasSize(1);
    }

    @Test
    void deleteRole_ShouldInvokeService() {
        ResponseEntity<BaseResponse> result = controller.deleteRole(1);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isEqualTo(1);
        verify(rolesService).deleteRoleById(eq(1));
    }
}
