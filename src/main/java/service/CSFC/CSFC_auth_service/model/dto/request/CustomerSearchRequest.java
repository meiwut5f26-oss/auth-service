package service.CSFC.CSFC_auth_service.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;

@Data
public class CustomerSearchRequest {
    @Size(max = 255, message = "Tên tối đa 255 ký tự")
    private String name;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 255, message = "Email tối đa 255 ký tự")
    private String email;

    @Pattern(regexp = "^(?:\\+?\\d{1,3})?\\d{7,15}$", message = "Số điện thoại không hợp lệ")
    private String phone;
    private CustomerStatus status;

    @NotNull(message = "Trang không được bỏ trống")
    @Min(value = 0, message = "Trang phải lớn hơn hoặc bằng 0")
    private Integer page = 0;

    @NotNull(message = "Kích thước trang không được bỏ trống")
    @Min(value = 1, message = "Kích thước trang phải lớn hơn 0")
    private Integer size = 10;

    @Size(max = 50, message = "sortBy tối đa 50 ký tự")
    private String sortBy = "createdAt";

    @Pattern(regexp = "(?i)asc|desc", message = "sortDir phải là asc hoặc desc")
    private String sortDir = "desc";
}
