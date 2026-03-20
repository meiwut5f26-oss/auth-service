package service.CSFC.CSFC_auth_service.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;

import java.util.UUID;

@Data
public class InternalCustomerCreateRequest {
    @NotBlank(message = "Họ và tên không được để trống")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Size(min = 8, max = 64, message = "Mật khẩu phải từ 8-64 ký tự")
    private String password;

    private String phone;
    private String address;
    private UUID franchiseId;
    private Boolean marketingOptin;
    private CustomerStatus status;
}

