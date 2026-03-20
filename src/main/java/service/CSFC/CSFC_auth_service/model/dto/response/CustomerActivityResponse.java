package service.CSFC.CSFC_auth_service.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import service.CSFC.CSFC_auth_service.common.client.dto.ExternalOrderResponse;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerActivityResponse {
    private UserResponse profile;
    private List<ExternalOrderResponse> orders;
}

