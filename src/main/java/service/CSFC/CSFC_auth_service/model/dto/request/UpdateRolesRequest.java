package service.CSFC.CSFC_auth_service.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRolesRequest {
    @NotNull(message = "id không được để trống")
    @Positive(message = "id phải lớn hơn 0")
    private Integer id;

    @NotBlank(message = "roleName không được để trống")
    @Size(max = 100, message = "roleName tối đa 100 ký tự")
    private String roleName;

}
