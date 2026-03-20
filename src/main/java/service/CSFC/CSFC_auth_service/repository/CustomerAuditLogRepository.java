package service.CSFC.CSFC_auth_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import service.CSFC.CSFC_auth_service.model.entity.CustomerAuditLog;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerAuditLogRepository extends JpaRepository<CustomerAuditLog, UUID> {
    List<CustomerAuditLog> findTop50ByUserIdOrderByCreatedAtDesc(UUID userId);
}

