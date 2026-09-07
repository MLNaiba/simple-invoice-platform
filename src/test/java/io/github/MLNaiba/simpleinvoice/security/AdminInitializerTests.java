package io.github.MLNaiba.simpleinvoice.security;

import io.github.MLNaiba.simpleinvoice.domain.User;
import io.github.MLNaiba.simpleinvoice.domain.UserRole;
import io.github.MLNaiba.simpleinvoice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminInitializerTests {

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "encoded-password";

    @Mock
    private ApplicationArguments args;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminProperties adminProperties;

    @InjectMocks
    private AdminInitializer adminInitializer;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    public void run_ifAdminDoesNotExist_thenCreateNewAdmin() {

        // ARRANGE

        when(userRepository.existsByRole(UserRole.ADMIN))
                .thenReturn(false);

        when(adminProperties.username())
                .thenReturn(USERNAME);

        when(adminProperties.password())
                .thenReturn(PASSWORD);

        when(passwordEncoder.encode(PASSWORD))
                .thenReturn(ENCODED_PASSWORD);

        // ACT

        adminInitializer.run(args);

        // ASSERT

        verify(userRepository).existsByRole(UserRole.ADMIN);

        verify(passwordEncoder).encode(PASSWORD);

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser)
                .extracting(
                        User::getUsername,
                        User::getPassword,
                        User::getRole
                )
                .containsExactly(
                        USERNAME,
                        ENCODED_PASSWORD,
                        UserRole.ADMIN
                );
    }

    @Test
    public void run_ifAdminExists_thenDoNotCreateNewAdmin() {

        // ARRANGE

        when(userRepository.existsByRole(UserRole.ADMIN))
                .thenReturn(true);

        // ACT

        adminInitializer.run(args);

        // ASSERT

        verify(userRepository).existsByRole(UserRole.ADMIN);

        verify(passwordEncoder, never()).encode(any(String.class));

        verify(userRepository, never()).save(any(User.class));
    }
}
