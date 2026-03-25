package service.CSFC.CSFC_auth_service.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminPermissionsCreateRequest {
    @NotBlank(message = "Tên permission không được để trống")
    @Size(max = 100, message = "Tên permission tối đa 100 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    @NotNull(message = "Ngày tạo không được để trống")
    private LocalDateTime creationDate;
}
