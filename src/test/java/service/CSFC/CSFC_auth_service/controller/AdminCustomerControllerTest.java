package service.CSFC.CSFC_auth_service.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import service.CSFC.CSFC_auth_service.common.response.BaseResponse;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;
import service.CSFC.CSFC_auth_service.model.dto.request.AdminUpdateCustomerProfileRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.CustomerSearchRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.CustomerStatusUpdateRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.CustomerActivityResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.CustomerAuditLogResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.UserResponse;
import service.CSFC.CSFC_auth_service.service.CustomerService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCustomerControllerTest {

    @Mock
    CustomerService customerService;

    @InjectMocks
    AdminCustomerController controller;

    private static final UserResponse SAMPLE_USER = UserResponse.builder()
            .id(UUID.randomUUID())
            .email("customer@example.com")
            .name("Customer")
            .status(CustomerStatus.ACTIVE)
            .build();

    @Test
    void getAllProfiles_ShouldReturnList() {
        when(customerService.getAllCustomers()).thenReturn(List.of(SAMPLE_USER));

        ResponseEntity<BaseResponse<List<UserResponse>>> response = controller.getAllProfiles();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(1);
    }

    @Test
    void getProfile_ShouldReturnUser() {
        UUID userId = UUID.randomUUID();
        when(customerService.getCustomerProfile(userId)).thenReturn(SAMPLE_USER);

        ResponseEntity<BaseResponse<UserResponse>> response = controller.getProfile(userId);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getEmail()).isEqualTo("customer@example.com");
    }

    @Test
    void updateProfile_ShouldReturnUpdatedUser() {
        UUID userId = UUID.randomUUID();
        AdminUpdateCustomerProfileRequest request = new AdminUpdateCustomerProfileRequest();
        request.setName("New Name");
        request.setMail("customer@example.com");

        when(customerService.adminUpdateCustomerProfile(eq(userId), any(AdminUpdateCustomerProfileRequest.class)))
                .thenReturn(SAMPLE_USER);

        ResponseEntity<BaseResponse<UserResponse>> response = controller.updateProfile(userId, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Cập nhật thông tin khách hàng");
    }

    @Test
    void updateStatus_ShouldInvokeService() {
        UUID userId = UUID.randomUUID();
        CustomerStatusUpdateRequest request = new CustomerStatusUpdateRequest();
        request.setStatus(CustomerStatus.LOCKED);

        ResponseEntity<BaseResponse<Void>> response = controller.updateStatus(userId, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(customerService).updateCustomerStatus(eq(userId), eq(CustomerStatus.LOCKED));
    }

    @Test
    void search_ShouldReturnPage() {
        CustomerSearchRequest request = new CustomerSearchRequest();
        request.setPage(0);
        request.setSize(10);
        Page<UserResponse> page = new PageImpl<>(List.of(SAMPLE_USER));
        when(customerService.searchCustomers(any(CustomerSearchRequest.class))).thenReturn(page);

        ResponseEntity<BaseResponse<Page<UserResponse>>> response = controller.search(request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getContent()).hasSize(1);
    }

    @Test
    void getActivity_ShouldReturnActivity() {
        UUID userId = UUID.randomUUID();
        CustomerActivityResponse activityResponse = new CustomerActivityResponse(SAMPLE_USER, List.of());
        when(customerService.getCustomerActivity(userId)).thenReturn(activityResponse);

        ResponseEntity<BaseResponse<CustomerActivityResponse>> response = controller.getActivity(userId);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getProfile().getEmail()).isEqualTo("customer@example.com");
    }

    @Test
    void getAuditLogs_ShouldReturnLogs() {
        UUID userId = UUID.randomUUID();
        when(customerService.getCustomerAuditLogs(userId)).thenReturn(List.of(new CustomerAuditLogResponse()));

        ResponseEntity<BaseResponse<List<CustomerAuditLogResponse>>> response = controller.getAuditLogs(userId);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(1);
    }

    @Test
    void lockCustomer_ShouldInvokeService() {
        UUID userId = UUID.randomUUID();

        ResponseEntity<BaseResponse<Void>> response = controller.lockCustomer(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(customerService).lockCustomer(eq(userId));
    }

    @Test
    void unlockCustomer_ShouldInvokeService() {
        UUID userId = UUID.randomUUID();

        ResponseEntity<BaseResponse<Void>> response = controller.unlockCustomer(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(customerService).unlockCustomer(eq(userId));
    }
}
