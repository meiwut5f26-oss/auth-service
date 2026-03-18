package service.CSFC.CSFC_auth_service.common.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import service.CSFC.CSFC_auth_service.model.entity.Roles;
import service.CSFC.CSFC_auth_service.model.entity.Users;
import service.CSFC.CSFC_auth_service.repository.RolesRepository;
import service.CSFC.CSFC_auth_service.repository.UsersRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private RolesRepository rolesRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    private ApplicationArguments appArgs;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dataInitializer, "name", "System Admin");
        ReflectionTestUtils.setField(dataInitializer, "roleName", "ADMIN");
        appArgs = mock(ApplicationArguments.class);
    }

    @Test
    void shouldSkipWhenDisabled() {
        ReflectionTestUtils.setField(dataInitializer, "enabled", false);

        dataInitializer.run(appArgs);

        verify(usersRepository, never()).existsByEmail(any());
        verify(usersRepository, never()).save(any());
    }

    @Test
    void shouldCreateAdminUserWhenEnabledAndNotExists() {
        ReflectionTestUtils.setField(dataInitializer, "enabled", true);
        ReflectionTestUtils.setField(dataInitializer, "email", "ADMIN@CSFC.LOCAL");
        ReflectionTestUtils.setField(dataInitializer, "password", "plain-password");

        when(usersRepository.existsByEmail("admin@csfc.local")).thenReturn(false);
        when(rolesRepository.findByName("ADMIN")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(rolesRepository.save(any(Roles.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usersRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dataInitializer.run(appArgs);

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(usersRepository).save(userCaptor.capture());

        Users seeded = userCaptor.getValue();
        assertEquals("admin@csfc.local", seeded.getEmail());
        assertEquals("encoded-password", seeded.getPassword());
        assertEquals("System Admin", seeded.getName());
        assertEquals(true, seeded.getIsActive());
    }
}
