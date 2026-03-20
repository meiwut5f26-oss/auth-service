package service.CSFC.CSFC_auth_service.service.imp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import service.CSFC.CSFC_auth_service.common.client.OrderClient;
import service.CSFC.CSFC_auth_service.common.client.dto.ExternalOrderResponse;
import service.CSFC.CSFC_auth_service.common.exception.BadRequestException;
import service.CSFC.CSFC_auth_service.common.exception.ResourceNotFoundException;
import service.CSFC.CSFC_auth_service.mapper.UserMapper;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;
import service.CSFC.CSFC_auth_service.model.dto.request.AdminUpdateCustomerProfileRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.CustomerSearchRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.UpdateMyProfileRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.InternalCustomerCreateRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.CustomerActivityResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.CustomerAuditLogResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.UserResponse;
import service.CSFC.CSFC_auth_service.model.entity.CustomerAuditLog;
import service.CSFC.CSFC_auth_service.model.entity.Roles;
import service.CSFC.CSFC_auth_service.model.entity.Users;
import service.CSFC.CSFC_auth_service.model.util.PasswordUtil;
import service.CSFC.CSFC_auth_service.repository.CustomerAuditLogRepository;
import service.CSFC.CSFC_auth_service.repository.RolesRepository;
import service.CSFC.CSFC_auth_service.repository.UsersRepository;
import service.CSFC.CSFC_auth_service.service.CustomerService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final OrderClient orderClient;
    private final CustomerAuditLogRepository auditLogRepository;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8,15}$");

    @Override
    public UserResponse getMyProfile(UUID userId) {
        Users user = getUserOrThrow(userId);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateMyProfile(UUID userId, UpdateMyProfileRequest request) {
        Users user = getUserOrThrow(userId);
        applyProfileUpdate(user, request.getName(), request.getPhone(), request.getAddress(), request.getMarketingOptin(), null);
        Users saved = usersRepository.save(user);
        logAudit(userId, "CUSTOMER_SELF_UPDATE", "Khách hàng tự cập nhật hồ sơ");
        return userMapper.toResponse(saved);
    }

    @Override
    public List<UserResponse> getAllCustomers() {
        return usersRepository.findAll().stream()
                .filter(this::isCustomer)
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse getCustomerProfile(UUID userId) {
        return userMapper.toResponse(getUserOrThrow(userId));
    }

    @Override
    @Transactional
    public UserResponse adminUpdateCustomerProfile(UUID userId, AdminUpdateCustomerProfileRequest request) {
        Users user = getUserOrThrow(userId);
        applyProfileUpdate(user, request.getName(), request.getPhone(), request.getAddress(), request.getMarketingOptin(), request.getFranchiseId());
        Users saved = usersRepository.save(user);
        logAudit(userId, "ADMIN_UPDATE_PROFILE", "Admin cập nhật hồ sơ khách hàng");
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void updateCustomerStatus(UUID userId, CustomerStatus status) {
        Users user = getUserOrThrow(userId);
        user.setStatus(status);
        usersRepository.save(user);
        logAudit(userId, "STATUS_CHANGE", "Thay đổi trạng thái: " + status);
    }

    @Override
    @Transactional
    public void lockCustomer(UUID userId) {
        Users user = getUserOrThrow(userId);
        if (user.getStatus() == CustomerStatus.LOCKED) {
            throw new BadRequestException("Tài khoản đã bị khóa");
        }
        user.setStatus(CustomerStatus.LOCKED);
        usersRepository.save(user);
        logAudit(userId, "LOCK", "Khóa tài khoản khách hàng");
    }

    @Override
    @Transactional
    public void unlockCustomer(UUID userId) {
        Users user = getUserOrThrow(userId);
        user.setStatus(CustomerStatus.ACTIVE);
        usersRepository.save(user);
        logAudit(userId, "UNLOCK", "Mở khóa tài khoản khách hàng");
    }

    @Override
    public Page<UserResponse> searchCustomers(CustomerSearchRequest request) {
        String status = request.getStatus() != null ? request.getStatus().name() : null;
        Sort sort = "desc".equalsIgnoreCase(request.getSortDir())
                ? Sort.by(request.getSortBy()).descending()
                : Sort.by(request.getSortBy()).ascending();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        return usersRepository.searchUsers(
                        normalize(request.getName()),
                        normalize(request.getEmail()),
                        normalize(request.getPhone()),
                        status,
                        true,
                        pageable)
                .map(userMapper::toResponse);
    }

    @Override
    public CustomerActivityResponse getCustomerActivity(UUID userId) {
        Users user = getUserOrThrow(userId);
        List<ExternalOrderResponse> orders = Collections.emptyList();
        try {
            orders = orderClient.getOrdersByCustomer(userId.toString());
        } catch (Exception ex) {
            log.warn("Không thể lấy lịch sử đơn hàng cho user {}: {}", userId, ex.getMessage());
        }
        return new CustomerActivityResponse(userMapper.toResponse(user), orders);
    }

    @Override
    public List<CustomerAuditLogResponse> getCustomerAuditLogs(UUID userId) {
        return auditLogRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(log -> new CustomerAuditLogResponse(log.getAction(), log.getDetail(), log.getCreatedAt()))
                .toList();
    }

    @Override
    public UserResponse getInternalCustomer(UUID userId) {
        return userMapper.toResponse(getUserOrThrow(userId));
    }

    @Override
    @Transactional
    public UserResponse updateInternalCustomer(UUID userId, AdminUpdateCustomerProfileRequest request) {
        Users user = getUserOrThrow(userId);
        applyProfileUpdate(user, request.getName(), request.getPhone(), request.getAddress(), request.getMarketingOptin(), request.getFranchiseId());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        Users saved = usersRepository.save(user);
        logAudit(userId, "INTERNAL_UPDATE", "Service nội bộ cập nhật hồ sơ khách hàng");
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse createInternalCustomer(InternalCustomerCreateRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (usersRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Email đã tồn tại");
        }

        Roles customerRole = rolesRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role CUSTOMER"));

        String rawPassword = StringUtils.hasText(request.getPassword())
                ? request.getPassword()
                : PasswordUtil.generateRandomPassword(12);

        String normalizedPhone = normalizePhone(request.getPhone());
        if (StringUtils.hasText(normalizedPhone) && usersRepository.existsByPhone(normalizedPhone)) {
            throw new BadRequestException("Số điện thoại đã tồn tại");
        }

        Users user = new Users();
        user.setName(request.getName());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPhone(normalizedPhone);
        user.setAddress(request.getAddress());
        user.setFranchiseId(request.getFranchiseId());
        user.setMarketingOptin(Boolean.TRUE.equals(request.getMarketingOptin()));
        user.setStatus(request.getStatus() != null ? request.getStatus() : CustomerStatus.ACTIVE);
        user.setIsFirstLogin(true);
        user.setRole(customerRole);

        Users saved = usersRepository.save(user);
        logAudit(saved.getId(), "INTERNAL_CREATE", "Service nội bộ tạo khách hàng");
        return userMapper.toResponse(saved);
    }

    private void logAudit(UUID userId, String action, String detail) {
        CustomerAuditLog log = new CustomerAuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setDetail(detail);
        auditLogRepository.save(log);
    }

    private Users getUserOrThrow(UUID userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    private void applyProfileUpdate(Users user,
                                    String name,
                                    String phone,
                                    String address,
                                    Boolean marketingOptin,
                                    UUID franchiseId) {
        if (StringUtils.hasText(name)) {
            user.setName(name.trim());
        }
        if (StringUtils.hasText(phone)) {
            String normalizedPhone = normalizePhone(phone);
            if (StringUtils.hasText(normalizedPhone)) {
                ensurePhoneValid(normalizedPhone);
                ensurePhoneUnique(normalizedPhone, user.getId());
                user.setPhone(normalizedPhone);
            }
        }
        if (StringUtils.hasText(address)) {
            user.setAddress(address.trim());
        }
        if (marketingOptin != null) {
            user.setMarketingOptin(marketingOptin);
        }
        if (franchiseId != null) {
            user.setFranchiseId(franchiseId);
        }
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizePhone(String rawPhone) {
        if (!StringUtils.hasText(rawPhone)) {
            return null;
        }
        return rawPhone.replaceAll("\\s+", "").trim();
    }

    private void ensurePhoneValid(String phone) {
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new BadRequestException("Số điện thoại không hợp lệ (chỉ bao gồm 8-15 chữ số)");
        }
    }

    private void ensurePhoneUnique(String phone, UUID currentUserId) {
        boolean exists = currentUserId == null
                ? usersRepository.existsByPhone(phone)
                : usersRepository.existsByPhoneAndIdNot(phone, currentUserId);
        if (exists) {
            throw new BadRequestException("Số điện thoại đã được sử dụng");
        }
    }

    private boolean isCustomer(Users user) {
        return user.getRole() != null && "CUSTOMER".equalsIgnoreCase(user.getRole().getName());
    }
}
