package service.CSFC.CSFC_auth_service.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import service.CSFC.CSFC_auth_service.common.response.BaseResponse;
import service.CSFC.CSFC_auth_service.model.dto.request.AdminPermissionCreateRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.AdminPermissionsViewResponse;
import service.CSFC.CSFC_auth_service.service.AdminPermissionsService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Admin - Permissions Management", description = "Admin quản lý permissions và gán permission cho role")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth-service/admin/roles")
public class AdminPermissionsController {

    private final AdminPermissionsService adminPermissionsService;

    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('PERMISSION_ASSIGN')")
    public ResponseEntity<BaseResponse<Object>> addPermissionToRole(
            @PathVariable Integer roleId,
            @RequestParam @NotBlank String permissionName
    ) {
        adminPermissionsService.addPermissionToRole(roleId, permissionName.trim().toUpperCase());
        return ResponseEntity.ok(BaseResponse.success("Gán permission cho role thành công", null));
    }

    @PostMapping("/permissions")
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    public ResponseEntity<BaseResponse<AdminPermissionsViewResponse>> createPermission(
            @Valid @RequestBody AdminPermissionCreateRequest request
    ) {
        AdminPermissionsViewResponse response = adminPermissionsService.createPermission(
                request.getName(),
                request.getDescription()
        );
        return ResponseEntity.ok(BaseResponse.success("Tạo permission thành công", response));
    }

    @DeleteMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    public ResponseEntity<BaseResponse<Object>> removePermissionFromRole(
            @PathVariable Integer roleId,
            @RequestParam @NotBlank String permissionName
    ) {
        adminPermissionsService.removePermissionFromRole(roleId, permissionName.trim().toUpperCase());
        return ResponseEntity.ok(BaseResponse.success("Xóa permission khỏi role thành công", null));
    }

    @DeleteMapping("/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    public ResponseEntity<BaseResponse<Object>> deletePermission(
            @PathVariable Integer permissionId
    ) {
        adminPermissionsService.deletePermission(permissionId);
        return ResponseEntity.ok(BaseResponse.success("Xóa permission thành công", null));
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    public ResponseEntity<BaseResponse<List<AdminPermissionsViewResponse>>> getAllPermissions() {

        List<AdminPermissionsViewResponse> permissions =
                adminPermissionsService.getAllPermissions();

        return ResponseEntity.ok(
                BaseResponse.success("Lấy danh sách permission thành công", permissions)
        );
    }

    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    public ResponseEntity<BaseResponse<List<AdminPermissionsViewResponse>>> getAllPermissionsByRole(
            @PathVariable Integer roleId
    ){
        List<AdminPermissionsViewResponse> permissions =
                adminPermissionsService.getAllPermissionsByRole(roleId);
        return ResponseEntity.ok(BaseResponse.success("Lấy danh sách permission của role thành công", permissions));
    }

}