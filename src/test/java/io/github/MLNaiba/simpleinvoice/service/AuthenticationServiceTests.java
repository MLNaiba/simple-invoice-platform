package io.github.MLNaiba.simpleinvoice.service;

import io.github.MLNaiba.simpleinvoice.dto.AuthenticationRequest;
import io.github.MLNaiba.simpleinvoice.dto.AuthenticationResponse;
import io.github.MLNaiba.simpleinvoice.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTests {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "password-encoded";
    private static final String USER_ROLE = "USER";
    private static final String TOKEN = "token";

    private UserDetails userDetails;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Captor
    private ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor;

    @Test
    public void authenticate_givenValidCredentials_shouldReturnResponseWithToken() {

        // ARRANGE

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(jwtService.generateToken(userDetails))
                .thenReturn(TOKEN);

        // ACT

        AuthenticationResponse response = authenticationService.authenticate(
                new AuthenticationRequest(USERNAME, PASSWORD));

        // ASSERT

        assertThat(response.token()).isEqualTo(TOKEN);

        verify(authenticationManager).authenticate(tokenCaptor.capture());
        UsernamePasswordAuthenticationToken capturedToken = tokenCaptor.getValue();

        assertThat(capturedToken.getPrincipal()).isEqualTo(USERNAME);
        assertThat(capturedToken.getCredentials()).isEqualTo(PASSWORD);

        verify(jwtService).generateToken(userDetails);
    }

    @Test
    public void authenticate_givenInvalidCredentials_shouldThrowBadCredentialsException() {

        // ARRANGE

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // ACT / ASSERT

        assertThatThrownBy(() ->
                authenticationService.authenticate(
                        new AuthenticationRequest(USERNAME, PASSWORD)))
                .isInstanceOf(BadCredentialsException.class);

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class));

        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

    // ---

    @BeforeEach
    public void setUp() {
        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .roles(USER_ROLE)
                .build();
    }
}
