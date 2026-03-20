package service.CSFC.CSFC_auth_service.service;

import org.springframework.data.domain.Page;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;
import service.CSFC.CSFC_auth_service.model.dto.request.AdminUpdateCustomerProfileRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.CustomerSearchRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.UpdateMyProfileRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.InternalCustomerCreateRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.CustomerActivityResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.CustomerAuditLogResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    UserResponse getMyProfile(UUID userId);

    UserResponse updateMyProfile(UUID userId, UpdateMyProfileRequest request);

    List<UserResponse> getAllCustomers();

    UserResponse getCustomerProfile(UUID userId);

    UserResponse adminUpdateCustomerProfile(UUID userId, AdminUpdateCustomerProfileRequest request);

    void updateCustomerStatus(UUID userId, CustomerStatus status);

    void lockCustomer(UUID userId);

    void unlockCustomer(UUID userId);

    Page<UserResponse> searchCustomers(CustomerSearchRequest request);

    CustomerActivityResponse getCustomerActivity(UUID userId);

    List<CustomerAuditLogResponse> getCustomerAuditLogs(UUID userId);

    UserResponse getInternalCustomer(UUID userId);

    UserResponse updateInternalCustomer(UUID userId, AdminUpdateCustomerProfileRequest request);

    UserResponse createInternalCustomer(InternalCustomerCreateRequest request);
}
