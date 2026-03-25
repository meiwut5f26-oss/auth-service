package service.CSFC.CSFC_auth_service.model.dto.request;

import lombok.Data;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;

@Data
public class CustomerSearchRequest {
    private String name;

    private String email;

    private String phone;
    private CustomerStatus status;

    private Integer page = 0;

    private Integer size = 10;

    private String sortBy = "createdAt";

    private String sortDir = "desc";
}
