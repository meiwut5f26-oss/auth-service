package service.CSFC.CSFC_auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import service.CSFC.CSFC_auth_service.common.exception.GlobalExceptionHandler;
import service.CSFC.CSFC_auth_service.common.response.BaseResponse;
import service.CSFC.CSFC_auth_service.model.constants.CustomerStatus;
import service.CSFC.CSFC_auth_service.model.dto.request.LoginRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.RefreshTokenRequest;
import service.CSFC.CSFC_auth_service.model.dto.request.RegisterRequest;
import service.CSFC.CSFC_auth_service.model.dto.response.AuthResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.RegisterResponse;
import service.CSFC.CSFC_auth_service.model.dto.response.UserResponse;
import service.CSFC.CSFC_auth_service.service.AuthenticationService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private static final String BASE_PATH = "/api/auth-service/auth";

    @Mock
    AuthenticationService authenticationService;

    @InjectMocks
    AuthenticationController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Validator validator;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.afterPropertiesSet();
        validator = localValidatorFactoryBean;
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void register_ShouldReturnCreatedResponse() {
        RegisterRequest request = new RegisterRequest("Nguyen Van A", "user@example.com", "ChangeMe@123", null, null, UUID.randomUUID());
        RegisterResponse response = RegisterResponse.builder()
                .user(UserResponse.builder()
                        .id(UUID.randomUUID())
                        .email("user@example.com")
                        .name("Nguyen Van A")
                        .status(CustomerStatus.ACTIVE)
                        .build())
                .build();

        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(response);

        ResponseEntity<BaseResponse<RegisterResponse>> result = controller.register(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getUser().getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void login_ShouldReturnAuthTokens() {
        LoginRequest request = new LoginRequest("user@example.com", "ChangeMe@123");
        AuthResponse response = AuthResponse.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .expiresIn(3600L)
                .user(UserResponse.builder().email("user@example.com").build())
                .build();

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(response);

        ResponseEntity<BaseResponse<AuthResponse>> result = controller.login(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getAccessToken()).isEqualTo("access");
    }

    @Test
    void refreshToken_ShouldReturnNewTokens() {
        RefreshTokenRequest request = new RefreshTokenRequest("refreshTokenValue" + "x".repeat(20));
        AuthResponse response = AuthResponse.builder()
                .accessToken("newAccess")
                .refreshToken("newRefresh")
                .expiresIn(3600L)
                .build();

        when(authenticationService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

        ResponseEntity<BaseResponse<AuthResponse>> result = controller.refreshToken(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData().getRefreshToken()).isEqualTo("newRefresh");
    }

    @Test
    void logout_ShouldInvokeService() {
        String token = "Bearer test-token";

        ResponseEntity<BaseResponse<Void>> response = controller.logout(token);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(authenticationService).logout(eq(token));
    }

    @Test
    void register_ShouldRejectBlankName() throws Exception {
        RegisterRequest request = validRegisterRequest();
        request.setName(" ");

        performRegister(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message", containsString("name")));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void register_ShouldRejectInvalidEmail() throws Exception {
        RegisterRequest request = validRegisterRequest();
        request.setEmail("invalid-email");

        performRegister(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("email")));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void register_ShouldRejectPasswordTooShort() throws Exception {
        RegisterRequest request = validRegisterRequest();
        request.setPassword("short");

        performRegister(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("password")));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void register_ShouldRejectPasswordTooLong() throws Exception {
        RegisterRequest request = validRegisterRequest();
        request.setPassword("x".repeat(21));

        performRegister(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("password")));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void register_ShouldRejectInvalidPhone() throws Exception {
        RegisterRequest request = validRegisterRequest();
        request.setPhone("1234");

        performRegister(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("phone")));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void register_ShouldRejectAddressTooLong() throws Exception {
        RegisterRequest request = validRegisterRequest();
        request.setAddress("a".repeat(501));

        performRegister(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("address")));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void login_ShouldRejectBlankEmail() throws Exception {
        LoginRequest request = validLoginRequest();
        request.setEmail(" ");

        performPost(BASE_PATH + "/login", request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("email")));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void login_ShouldRejectInvalidEmailFormat() throws Exception {
        LoginRequest request = validLoginRequest();
        request.setEmail("invalid");

        performPost(BASE_PATH + "/login", request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("email")));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void login_ShouldRejectPasswordOutOfRange() throws Exception {
        LoginRequest request = validLoginRequest();
        request.setPassword("short");

        performPost(BASE_PATH + "/login", request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("password")));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void refresh_ShouldRejectBlankToken() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest(" ");

        performPost(BASE_PATH + "/refresh", request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("refreshToken")));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void refresh_ShouldRejectTooShortToken() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("short-token");

        performPost(BASE_PATH + "/refresh", request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("refreshToken")));

        verifyNoInteractions(authenticationService);
    }

    private RegisterRequest validRegisterRequest() {
        return new RegisterRequest(
                "Nguyen Van A",
                "user@example.com",
                "ChangeMe@123",
                "+84912345678",
                "123 CSFC Street",
                UUID.randomUUID()
        );
    }

    private LoginRequest validLoginRequest() {
        return new LoginRequest("user@example.com", "ChangeMe@123");
    }

    private ResultActions performRegister(RegisterRequest request) throws Exception {
        return performPost(BASE_PATH + "/register", request);
    }

    private ResultActions performPost(String path, Object payload) throws Exception {
        return mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)));
    }
}
