package io.github.MLNaiba.simpleinvoice.service;

import io.github.MLNaiba.simpleinvoice.domain.User;
import io.github.MLNaiba.simpleinvoice.domain.UserRole;
import io.github.MLNaiba.simpleinvoice.repository.UserRepository;
import io.github.MLNaiba.simpleinvoice.service.impl.CustomUserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceTests {

    private static final String ID = "id";
    private static final String USERNAME = "username";
    private static final String ENCODED_PASSWORD = "encoded-password";

    private static final String INVALID_USERNAME = "invalid-username";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsServiceImpl userDetailsService;

    @Test
    public void loadUserByUsername_givenValidRequest_shouldReturnUserDetails() {

        // ARRANGE

        User user = user();

        when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Optional.of(user));

        // ACT

        UserDetails userDetails = userDetailsService.loadUserByUsername(
                user.getUsername());

        // ASSERT

        verify(userRepository).findByUsername(user.getUsername());

        assertThat(userDetails.getUsername()).isEqualTo(user.getUsername());
        assertThat(userDetails.getPassword()).isEqualTo(user.getPassword());

        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_" + user.getRole().name());
    }

    @Test
    public void loadUserByUsername_givenInvalidRequestNoUser_shouldThrowUsernameNotFoundException() {

        // ARRANGE

        when(userRepository.findByUsername(INVALID_USERNAME))
                .thenReturn(Optional.empty());

        // ACT / ASSERT

        assertThatThrownBy(() ->
                userDetailsService.loadUserByUsername(INVALID_USERNAME))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(INVALID_USERNAME);

        verify(userRepository).findByUsername(INVALID_USERNAME);
    }

    // ---

    private User user() {
        return User.builder()
                .id(ID)
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .role(UserRole.USER)
                .build();
    }
}
