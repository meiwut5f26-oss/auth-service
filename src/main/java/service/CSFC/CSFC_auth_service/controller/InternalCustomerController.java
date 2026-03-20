package service.CSFC.CSFC_auth_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import service.CSFC.CSFC_auth_service.common.response.BaseResponse;
import service.CSFC.CSFC_auth_service.model.dto.request.AdminUpdateCustomerProfileRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.InternalCustomerCreateRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.UserResponse;
import service.CSFC.CSFC_auth_service.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "Internal - Customer Bridge", description = "Endpoint nội bộ cho các service khác")
@RestController
@RequestMapping("/api/auth-service/internal/customers")
@RequiredArgsConstructor
public class InternalCustomerController {

    private final CustomerService customerService;

    @GetMapping("/{userId}/details")
    @PreAuthorize("hasAuthority('INTERNAL_CUSTOMER_READ')")
    public ResponseEntity<BaseResponse<UserResponse>> getInternalCustomer(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                BaseResponse.success("Lấy thông tin khách hàng thành công", customerService.getInternalCustomer(userId))
        );
    }

    @PutMapping("/{userId}/details")
    @PreAuthorize("hasAuthority('INTERNAL_CUSTOMER_WRITE')")
    public ResponseEntity<BaseResponse<UserResponse>> updateInternalCustomer(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUpdateCustomerProfileRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success("Cập nhật thông tin khách hàng thành công", customerService.updateInternalCustomer(userId, request))
        );
    }

    @PostMapping("/bridge")
    @PreAuthorize("hasAuthority('INTERNAL_CUSTOMER_WRITE')")
    public ResponseEntity<BaseResponse<UserResponse>> createInternalCustomer(
            @Valid @RequestBody InternalCustomerCreateRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success("Tạo khách hàng thành công", customerService.createInternalCustomer(request))
        );
    }
}
