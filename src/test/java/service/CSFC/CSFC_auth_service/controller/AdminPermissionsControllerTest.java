package service.CSFC.CSFC_auth_service.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import service.CSFC.CSFC_auth_service.common.response.BaseResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.AdminPermissionsViewResponse;
import service.CSFC.CSFC_auth_service.service.AdminPermissionsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPermissionsControllerTest {

    @Mock
    AdminPermissionsService adminPermissionsService;

    @InjectMocks
    AdminPermissionsController controller;

    @Test
    void addPermission_ShouldInvokeService() {
        ResponseEntity<BaseResponse<Object>> response = controller.addPermissionToRole(1, "USER_READ");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(adminPermissionsService).addPermissionToRole(eq(1), eq("USER_READ"));
    }

    @Test
    void getAllPermissions_ShouldReturnList() {
        AdminPermissionsViewResponse viewResponse = AdminPermissionsViewResponse.builder()
                .id(1)
                .name("USER_READ")
                .description("Read access")
                .build();
        when(adminPermissionsService.getAllPermissions()).thenReturn(List.of(viewResponse));

        ResponseEntity<BaseResponse<List<AdminPermissionsViewResponse>>> response = controller.getAllPermissions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(1);
    }

    @Test
    void getPermissionsByRole_ShouldReturnList() {
        AdminPermissionsViewResponse viewResponse = AdminPermissionsViewResponse.builder()
                .id(2)
                .name("USER_WRITE")
                .description("Write access")
                .build();
        when(adminPermissionsService.getAllPermissionsByRole(1)).thenReturn(List.of(viewResponse));

        ResponseEntity<BaseResponse<List<AdminPermissionsViewResponse>>> response = controller.getAllPermissionsByRole(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(1);
    }
}
