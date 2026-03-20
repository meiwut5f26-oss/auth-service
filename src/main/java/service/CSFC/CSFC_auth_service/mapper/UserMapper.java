package service.CSFC.CSFC_auth_service.mapper;

import org.springframework.stereotype.Component;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;
import service.CSFC.CSFC_auth_service.model.dto.request.CreateUserRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.RegisterRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.RegisterResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.UserDetailResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.UserResponse;
import service.CSFC.CSFC_auth_service.model.entity.Users;

@Component
public class UserMapper {
    public Users toEntity(RegisterRequest request, String encodedPassword) {
        Users user = new Users();
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setName(request.getName());
        user.setFranchiseId(request.getFranchiseId());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());           // ← added
        user.setStatus(CustomerStatus.ACTIVE);       // ← replaces setIsActive
        user.setIsFirstLogin(true);
        user.setMarketingOptin(false);               // ← added
        return user;
    }

    public Users toEntityCreateUserWithRoleByAdmin(CreateUserRequest request, String encodedPassword) {
        Users user = new Users();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());
        user.setFranchiseId(request.getFranchiseId());   // ← added
        user.setPassword(encodedPassword);
        user.setStatus(CustomerStatus.ACTIVE);           // ← replaces setIsActive
        user.setMarketingOptin(false);
        return user;
    }
    public UserResponse toResponse(Users user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFranchiseId(user.getFranchiseId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setAddress(user.getAddress());
        response.setPhone(user.getPhone());          // ← added
        response.setStatus(user.getStatus());        // ← added
        response.setMarketingOptin(user.isMarketingOptin());
        response.setIsFirstLogin(user.getIsFirstLogin());
        response.setRole(user.getRole() != null ? user.getRole().getName() : null); // ← added
        return response;
    }

    public UserDetailResponse toDetailResponse(Users user) {
        UserDetailResponse response = new UserDetailResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole() != null ? user.getRole().getName() : null);
        response.setIsFirstLogin(user.getIsFirstLogin());
        response.setStatus(user.getStatus());
        response.setMarketingOptin(user.isMarketingOptin());
        return response;
    }
}
