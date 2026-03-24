package service.CSFC.CSFC_auth_service.service.imp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import service.CSFC.CSFC_auth_service.common.security.CustomerUserDetails;

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
    private final ObjectMapper objectMapper;
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
        String oldSnapshot = toJsonSafe(userMapper.toResponse(user));
        applyProfileUpdate(user, request.getName(), request.getPhone(), request.getAddress(), request.getMarketingOptin(), null,request.getMail());
        Users saved = usersRepository.save(user);
        logAudit(userId, "CUSTOMER_SELF_UPDATE", "Khách hàng tự cập nhật hồ sơ", oldSnapshot, toJsonSafe(userMapper.toResponse(saved)), "USER", userId);
        return userMapper.toResponse(saved);
    }

    @Override
    public List<UserResponse> getAllCustomers() {
        return usersRepository.findAll().stream()
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
        String oldSnapshot = toJsonSafe(userMapper.toResponse(user));
        applyProfileUpdate(user, request.getName(), request.getPhone(), request.getAddress(), request.getMarketingOptin(), request.getFranchiseId(),request.getMail());
        Users saved = usersRepository.save(user);
        logAudit(userId, "ADMIN_UPDATE_PROFILE", "Admin cập nhật hồ sơ khách hàng", oldSnapshot, toJsonSafe(userMapper.toResponse(saved)), "USER", userId);
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void updateCustomerStatus(UUID userId, CustomerStatus status) {
        Users user = getUserOrThrow(userId);
        String oldSnapshot = toJsonSafe(userMapper.toResponse(user));
        user.setStatus(status);
        usersRepository.save(user);
        logAudit(userId, "STATUS_CHANGE", "Thay đổi trạng thái: " + status, oldSnapshot, toJsonSafe(userMapper.toResponse(user)), "USER", userId);
    }

    @Override
    @Transactional
    public void lockCustomer(UUID userId) {
        Users user = getUserOrThrow(userId);
        if (user.getStatus() == CustomerStatus.LOCKED) {
            throw new BadRequestException("Tài khoản đã bị khóa");
        }
        String oldSnapshot = toJsonSafe(userMapper.toResponse(user));
        user.setStatus(CustomerStatus.LOCKED);
        usersRepository.save(user);
        logAudit(userId, "LOCK", "Khóa tài khoản khách hàng", oldSnapshot, toJsonSafe(userMapper.toResponse(user)), "USER", userId);
    }

    @Override
    @Transactional
    public void unlockCustomer(UUID userId) {
        Users user = getUserOrThrow(userId);
        String oldSnapshot = toJsonSafe(userMapper.toResponse(user));
        user.setStatus(CustomerStatus.ACTIVE);
        usersRepository.save(user);
        logAudit(userId, "UNLOCK", "Mở khóa tài khoản khách hàng", oldSnapshot, toJsonSafe(userMapper.toResponse(user)), "USER", userId);
    }

    @Override
    public Page<UserResponse> searchCustomers(CustomerSearchRequest request) {
        CustomerStatus status = request.getStatus();
        Sort sort = "desc".equalsIgnoreCase(request.getSortDir())
                ? Sort.by(request.getSortBy()).descending()
                : Sort.by(request.getSortBy()).ascending();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        List<UserResponse> filtered = usersRepository.findAll().stream()
                .filter(u -> matches(u.getName(), request.getName()))
                .filter(u -> matches(u.getEmail(), request.getEmail()))
                .filter(u -> matches(u.getPhone(), request.getPhone()))
                .filter(u -> status == null || status.equals(u.getStatus()))
                .map(userMapper::toResponse)
                .toList();

        filtered = filtered.stream()
                .sorted((a, b) -> sort.getOrderFor(request.getSortBy()).isAscending()
                        ? compareByField(a, b, request.getSortBy())
                        : compareByField(b, a, request.getSortBy()))
                .toList();

        int start = Math.min((int) pageable.getOffset(), filtered.size());
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<UserResponse> pageSlice = filtered.subList(start, end);

        return new PageImpl<>(pageSlice, pageable, filtered.size());
    }

    private boolean matches(String field, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        return field != null && field.toLowerCase().contains(keyword.trim().toLowerCase());
    }

    private int compareByField(UserResponse a, UserResponse b, String sortBy) {
        if ("createdAt".equalsIgnoreCase(sortBy)) {
            return 0; // createdAt not available in response; keep stable order
        }
        if ("name".equalsIgnoreCase(sortBy)) {
            return nullSafe(a.getName()).compareToIgnoreCase(nullSafe(b.getName()));
        }
        if ("email".equalsIgnoreCase(sortBy)) {
            return nullSafe(a.getEmail()).compareToIgnoreCase(nullSafe(b.getEmail()));
        }
        if ("phone".equalsIgnoreCase(sortBy)) {
            return nullSafe(a.getPhone()).compareToIgnoreCase(nullSafe(b.getPhone()));
        }
        return 0;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
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
                .map(log -> new CustomerAuditLogResponse(
                        log.getAction(),
                        log.getDetail(),
                        log.getCreatedAt(),
                        log.getActorId(),
                        log.getActorEmail(),
                        log.getEntity(),
                        log.getEntityId(),
                        log.getOldValue(),
                        log.getNewValue()))
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
        String oldSnapshot = toJsonSafe(userMapper.toResponse(user));
        applyProfileUpdate(user, request.getName(), request.getPhone(), request.getAddress(), request.getMarketingOptin(), request.getFranchiseId(),request.getMail());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        Users saved = usersRepository.save(user);
        logAudit(userId, "INTERNAL_UPDATE", "Service nội bộ cập nhật hồ sơ khách hàng", oldSnapshot, toJsonSafe(userMapper.toResponse(saved)), "USER", userId);
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
        logAudit(saved.getId(), "INTERNAL_CREATE", "Service nội bộ tạo khách hàng", null, toJsonSafe(userMapper.toResponse(saved)), "USER", saved.getId());
        return userMapper.toResponse(saved);
    }

    private void logAudit(UUID userId, String action, String detail, String oldValue, String newValue, String entity, UUID entityId) {
        CustomerAuditLog log = new CustomerAuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setDetail(detail);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setEntity(entity);
        log.setEntityId(entityId);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomerUserDetails principal) {
            log.setActorId(principal.getUser().getId());
            log.setActorEmail(principal.getUser().getEmail());
        }

        auditLogRepository.save(log);
    }

    private String toJsonSafe(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return null;
        }
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
                                    UUID franchiseId,String mail) {
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
        if(mail != null){
            user.setEmail(mail);
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
        return user.getRole() != null && "CUSTOMER".equalsIgnoreCase(normalizeRoleName(user.getRole().getName()));
    }

    private String normalizeRoleName(String roleName) {
        return roleName == null ? null : roleName.replaceAll("\\s+", "").toUpperCase();
    }
}
