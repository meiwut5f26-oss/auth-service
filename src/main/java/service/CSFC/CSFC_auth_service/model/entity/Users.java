package service.CSFC.CSFC_auth_service.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    private String name;
    private UUID franchiseId;
    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    private String password;

    private String address;

    @Column(unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status;

    private boolean marketingOptin;

    private Boolean isFirstLogin;

    @JsonIgnore
    private String refreshToken;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "id_role")
    private Roles role;
}
