package service.CSFC.CSFC_auth_service.service;

import service.CSFC.CSFC_auth_service.model.dto.response.AdminPermissionsViewResponse;

import java.util.List;

public interface AdminPermissionsService {
    void addPermissionToRole(Integer roleId, String permissionName);

    // Create a new permission without assigning it to a role
    AdminPermissionsViewResponse createPermission(String permissionName, String description);

    // Remove an existing permission from a role
    void removePermissionFromRole(Integer roleId, String permissionName);

    // Delete a permission entirely
    void deletePermission(Integer permissionId);

    List<AdminPermissionsViewResponse> getAllPermissions();

    List<AdminPermissionsViewResponse> getAllPermissionsByRole(Integer roleId);
}
