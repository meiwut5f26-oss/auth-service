package service.CSFC.CSFC_auth_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.CSFC.CSFC_auth_service.common.response.BaseResponse;
import service.CSFC.CSFC_auth_service.common.security.CustomerUserDetails;
import service.CSFC.CSFC_auth_service.model.dto.request.UpdateMyProfileRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.UserResponse;
import service.CSFC.CSFC_auth_service.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Customer Profile", description = "Khách hàng xem/cập nhật hồ sơ")
@RestController
@RequestMapping("/api/auth-service/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/me/details")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<BaseResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal CustomerUserDetails currentUser
    ) {
        UserResponse response = customerService.getMyProfile(currentUser.getUser().getId());
        return ResponseEntity.ok(BaseResponse.success("Lấy thông tin hồ sơ thành công", response));
    }

    @PutMapping("/me/details")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<BaseResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomerUserDetails currentUser,
            @Valid @RequestBody UpdateMyProfileRequest request
    ) {
        UserResponse response = customerService.updateMyProfile(currentUser.getUser().getId(), request);
        return ResponseEntity.ok(BaseResponse.success("Cập nhật hồ sơ thành công", response));
    }
}
