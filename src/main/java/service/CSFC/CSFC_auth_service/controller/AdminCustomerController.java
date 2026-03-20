package service.CSFC.CSFC_auth_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import service.CSFC.CSFC_auth_service.common.response.BaseResponse;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;
import service.CSFC.CSFC_auth_service.model.dto.request.AdminUpdateCustomerProfileRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.CustomerSearchRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.CustomerStatusUpdateRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.CustomerActivityResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.CustomerAuditLogResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.UserResponse;
import service.CSFC.CSFC_auth_service.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Tag(name = "Admin - Customer Management", description = "Quản lý hồ sơ khách hàng")
@RestController
@RequestMapping("/api/auth-service/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final CustomerService customerService;

    @GetMapping("/all-profile")
    @PreAuthorize("hasAuthority('CUSTOMER_PROFILE_LIST')")
    public ResponseEntity<BaseResponse<List<UserResponse>>> getAllProfiles() {
        return ResponseEntity.ok(
                BaseResponse.success("Lấy danh sách khách hàng thành công", customerService.getAllCustomers())
        );
    }

    @GetMapping("/{userId}/profile")
    @PreAuthorize("hasAuthority('CUSTOMER_PROFILE_VIEW')")
    public ResponseEntity<BaseResponse<UserResponse>> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                BaseResponse.success("Lấy thông tin khách hàng thành công", customerService.getCustomerProfile(userId))
        );
    }

    @PutMapping("/{userId}/profile")
    @PreAuthorize("hasAuthority('CUSTOMER_PROFILE_UPDATE')")
    public ResponseEntity<BaseResponse<UserResponse>> updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUpdateCustomerProfileRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success("Cập nhật thông tin khách hàng thành công", customerService.adminUpdateCustomerProfile(userId, request))
        );
    }

    @PatchMapping("/{userId}/profile/status")
    @PreAuthorize("hasAuthority('CUSTOMER_PROFILE_STATUS_UPDATE')")
    public ResponseEntity<BaseResponse<Void>> updateStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody CustomerStatusUpdateRequest request
    ) {
        customerService.updateCustomerStatus(userId, request.getStatus());
        return ResponseEntity.ok(BaseResponse.success("Cập nhật trạng thái thành công", null));
    }

    @PatchMapping("/{userId}/profile/lock")
    @PreAuthorize("hasAuthority('CUSTOMER_PROFILE_LOCK')")
    public ResponseEntity<BaseResponse<Void>> lockCustomer(@PathVariable UUID userId) {
        customerService.lockCustomer(userId);
        return ResponseEntity.ok(BaseResponse.success("Khóa tài khoản thành công", null));
    }

    @PatchMapping("/{userId}/profile/unlock")
    @PreAuthorize("hasAuthority('CUSTOMER_PROFILE_UNLOCK')")
    public ResponseEntity<BaseResponse<Void>> unlockCustomer(@PathVariable UUID userId) {
        customerService.unlockCustomer(userId);
        return ResponseEntity.ok(BaseResponse.success("Mở khóa tài khoản thành công", null));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('CUSTOMER_SEARCH')")
    public ResponseEntity<BaseResponse<Page<UserResponse>>> search(@Valid @RequestBody CustomerSearchRequest request) {
        return ResponseEntity.ok(
                BaseResponse.success("Tìm kiếm khách hàng thành công", customerService.searchCustomers(request))
        );
    }

    @GetMapping("/{userId}/activity")
    @PreAuthorize("hasAuthority('CUSTOMER_ACTIVITY_VIEW')")
    public ResponseEntity<BaseResponse<CustomerActivityResponse>> getActivity(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                BaseResponse.success("Lấy lịch sử hoạt động thành công", customerService.getCustomerActivity(userId))
        );
    }

    @GetMapping("/{userId}/audit")
    @PreAuthorize("hasAuthority('CUSTOMER_AUDIT_VIEW')")
    public ResponseEntity<BaseResponse<List<CustomerAuditLogResponse>>> getAuditLogs(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                BaseResponse.success("Lấy lịch sử audit thành công", customerService.getCustomerAuditLogs(userId))
        );
    }
}
