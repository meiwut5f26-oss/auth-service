package service.CSFC.CSFC_auth_service.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AssignRoleRequest {
    @NotNull(message = "roleId không được để trống")
    @Positive(message = "roleId phải lớn hơn 0")
    private Long roleId;
}
