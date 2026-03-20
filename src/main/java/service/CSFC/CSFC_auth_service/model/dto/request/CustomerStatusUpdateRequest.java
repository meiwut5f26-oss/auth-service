package service.CSFC.CSFC_auth_service.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;

@Data
public class CustomerStatusUpdateRequest {
    @NotNull(message = "Trạng thái không được bỏ trống")
    private CustomerStatus status;
}

