package service.CSFC.CSFC_auth_service.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import service.CSFC.CSFC_auth_service.common.response.BaseResponse;
import service.CSFC.CSFC_auth_service.model.dto.request.AdminUpdateCustomerProfileRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.InternalCustomerCreateRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.UserResponse;
import service.CSFC.CSFC_auth_service.service.CustomerService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalCustomerControllerTest {

    @Mock
    CustomerService customerService;

    @InjectMocks
    InternalCustomerController controller;

    @Test
    void getInternalCustomer_ShouldReturnDetails() {
        UUID userId = UUID.randomUUID();
        UserResponse response = UserResponse.builder().id(userId).email("customer@example.com").build();
        when(customerService.getInternalCustomer(userId)).thenReturn(response);

        ResponseEntity<BaseResponse<UserResponse>> result = controller.getInternalCustomer(userId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getEmail()).isEqualTo("customer@example.com");
    }

    @Test
    void updateInternalCustomer_ShouldReturnUpdatedUser() {
        UUID userId = UUID.randomUUID();
        AdminUpdateCustomerProfileRequest request = new AdminUpdateCustomerProfileRequest();
        request.setMail("customer@example.com");

        UserResponse response = UserResponse.builder().id(userId).email("customer@example.com").build();
        when(customerService.updateInternalCustomer(eq(userId), any(AdminUpdateCustomerProfileRequest.class)))
                .thenReturn(response);

        ResponseEntity<BaseResponse<UserResponse>> result = controller.updateInternalCustomer(userId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getEmail()).isEqualTo("customer@example.com");
    }

    @Test
    void createInternalCustomer_ShouldReturnCreatedUser() {
        InternalCustomerCreateRequest request = new InternalCustomerCreateRequest();
        request.setName("Customer");
        request.setEmail("customer@example.com");
        request.setPassword("ChangeMe@123");

        UserResponse response = UserResponse.builder().id(UUID.randomUUID()).email("customer@example.com").build();
        when(customerService.createInternalCustomer(any(InternalCustomerCreateRequest.class))).thenReturn(response);

        ResponseEntity<BaseResponse<UserResponse>> result = controller.createInternalCustomer(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getMessage()).contains("Tạo khách hàng thành công");
    }
}
