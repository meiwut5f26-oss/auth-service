package service.CSFC.CSFC_auth_service.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import service.CSFC.CSFC_auth_service.common.response.BaseResponse;
import service.CSFC.CSFC_auth_service.common.security.CustomerUserDetails;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;
import service.CSFC.CSFC_auth_service.model.dto.request.CreateUserRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.UpdateUserRoleRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.UserResponse;
import service.CSFC.CSFC_auth_service.service.UserService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    UserService userService;

    @InjectMocks
    UserController controller;

    private static final CustomerUserDetails ADMIN_PRINCIPAL =
            TestSecurityUtils.customerPrincipal("ADMIN", "USER_READ_SELF", "USER_CREATE", "USER_DELETE", "USER_UPDATE_STATUS", "USER_UPDATE_ROLE");

    @Test
    void getCurrentUser_ShouldReturnProfile() {
        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .name("User")
                .status(CustomerStatus.ACTIVE)
                .build();
        when(userService.getCurrentUser("user@csfc.local")).thenReturn(response);

        ResponseEntity<BaseResponse<UserResponse>> result = controller.getCurrentUser(ADMIN_PRINCIPAL);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void deleteUser_ShouldInvokeService() {
        UUID userId = UUID.randomUUID();

        ResponseEntity<BaseResponse<String>> result = controller.deleteUser(userId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userService).deleteUserByAdmin(eq(userId));
    }

    @Test
    void deactivateUser_ShouldInvokeService() {
        UUID userId = UUID.randomUUID();

        ResponseEntity<BaseResponse<String>> result = controller.deactivateUser(userId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userService).deActivateUserByAdmin(eq(userId));
    }

    @Test
    void createAccount_ShouldReturnCreatedUser() {
        CreateUserRequest request = new CreateUserRequest("staff@example.com", "Staff", "123 road", "0123456789", UUID.randomUUID(), "STAFF");
        UserResponse response = UserResponse.builder()
                .id(UUID.randomUUID())
                .email("staff@example.com")
                .build();
        when(userService.createUserWithRoleByAdmin(any(CreateUserRequest.class))).thenReturn(response);

        ResponseEntity<BaseResponse<UserResponse>> result = controller.createAccountByAdmin(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getEmail()).isEqualTo("staff@example.com");
    }

    @Test
    void updateUserRole_ShouldInvokeService() {
        UUID userId = UUID.randomUUID();
        UpdateUserRoleRequest request = new UpdateUserRoleRequest("MANAGER");

        ResponseEntity<BaseResponse<String>> result = controller.updateUserRole(userId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getMessage()).contains("Cập nhật role");
        verify(userService).updateUserRoleByAdmin(eq(userId), eq("MANAGER"));
    }
}
