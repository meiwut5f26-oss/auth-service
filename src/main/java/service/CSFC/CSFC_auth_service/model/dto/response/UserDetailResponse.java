package service.CSFC.CSFC_auth_service.model.dto.response;

import lombok.Data;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;

import java.util.UUID;

@Data
public class UserDetailResponse {
    private UUID id;
    private String email;
    private String role;
    private Boolean isFirstLogin;
    private CustomerStatus status;
    private boolean marketingOptin;
}