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
import service.CSFC.CSFC_auth_service.model.dto.request.UpdateMyProfileRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.UserResponse;
import service.CSFC.CSFC_auth_service.service.CustomerService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    CustomerService customerService;

    @InjectMocks
    CustomerController controller;

    private static final CustomerUserDetails CUSTOMER_PRINCIPAL =
            TestSecurityUtils.customerPrincipal("CUSTOMER", "CUSTOMER_PROFILE_VIEW");

    @Test
    void getMyProfile_ShouldReturnProfile() {
        UUID userId = CUSTOMER_PRINCIPAL.getUser().getId();
        UserResponse response = UserResponse.builder()
                .id(userId)
                .email("customer@example.com")
                .status(CustomerStatus.ACTIVE)
                .build();
        when(customerService.getMyProfile(userId)).thenReturn(response);

        ResponseEntity<BaseResponse<UserResponse>> result = controller.getMyProfile(CUSTOMER_PRINCIPAL);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getEmail()).isEqualTo("customer@example.com");
    }

    @Test
    void updateMyProfile_ShouldReturnUpdatedProfile() {
        UUID userId = CUSTOMER_PRINCIPAL.getUser().getId();
        UpdateMyProfileRequest request = new UpdateMyProfileRequest();
        request.setName("New Name");
        request.setMail("customer@example.com");

        UserResponse response = UserResponse.builder()
                .id(userId)
                .email("customer@example.com")
                .name("New Name")
                .build();
        when(customerService.updateMyProfile(eq(userId), any(UpdateMyProfileRequest.class))).thenReturn(response);

        ResponseEntity<BaseResponse<UserResponse>> result = controller.updateMyProfile(CUSTOMER_PRINCIPAL, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getName()).isEqualTo("New Name");
    }
}
