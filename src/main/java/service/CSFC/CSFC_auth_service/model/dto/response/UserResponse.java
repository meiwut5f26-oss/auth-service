package service.CSFC.CSFC_auth_service.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID id;
    private UUID franchiseId;
    private String name;
    private String email;
    private String address;
    private String phone;
    private CustomerStatus status;
    private boolean marketingOptin;
    private Boolean isFirstLogin;
    private String role;
}