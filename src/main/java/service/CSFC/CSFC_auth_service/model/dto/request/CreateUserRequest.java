package service.CSFC.CSFC_auth_service.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ, vui lòng nhập lại")
    @Size(max = 255, message = "Email tối đa 255 ký tự")
    private String email;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 255, message = "Họ và tên tối đa 255 ký tự")
    private String name;

    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    private String address;

    @Pattern(regexp = "^(?:\\+?\\d{1,3})?\\d{7,15}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    private UUID franchiseId;   // required when creating STAFF

    @NotBlank(message = "Role không được để trống")
    @Size(max = 100, message = "Role tối đa 100 ký tự")
    private String roleName;    // role identifier, e.g. "STAFF" or "ADMIN"
}
