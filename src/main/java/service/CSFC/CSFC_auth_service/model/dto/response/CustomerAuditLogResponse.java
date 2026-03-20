package service.CSFC.CSFC_auth_service.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAuditLogResponse {
    private String action;
    private String detail;
    private LocalDateTime createdAt;
    private UUID actorId;
    private String actorEmail;
    private String entity;
    private UUID entityId;
    private String oldValue;
    private String newValue;
}
