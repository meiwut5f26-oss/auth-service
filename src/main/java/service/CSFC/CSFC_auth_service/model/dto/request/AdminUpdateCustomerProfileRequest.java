package service.CSFC.CSFC_auth_service.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;

import java.util.UUID;

@Data
public class AdminUpdateCustomerProfileRequest {
    @Size(max = 255, message = "Họ và tên tối đa 255 ký tự")
    private String name;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone;

    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    private String address;

    @Email
    private String mail;
    private Boolean marketingOptin;

    private UUID franchiseId;

    private CustomerStatus status;
}
