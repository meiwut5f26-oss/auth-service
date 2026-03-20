package service.CSFC.CSFC_auth_service.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;

@Data
public class CustomerSearchRequest {
    private String name;
    private String email;
    private String phone;
    private CustomerStatus status;

    @NotNull(message = "Trang không được bỏ trống")
    @Min(value = 0, message = "Trang phải lớn hơn hoặc bằng 0")
    private Integer page = 0;

    @NotNull(message = "Kích thước trang không được bỏ trống")
    @Min(value = 1, message = "Kích thước trang phải lớn hơn 0")
    private Integer size = 10;

    private String sortBy = "createdAt";

    @Pattern(regexp = "(?i)asc|desc", message = "sortDir phải là asc hoặc desc")
    private String sortDir = "desc";
}

