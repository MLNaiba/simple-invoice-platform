package io.github.MLNaiba.simpleinvoice.security;

import io.github.MLNaiba.simpleinvoice.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTests {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String USER_ROLE = "USER";
    private static final String TOKEN = "token";

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails userDetails;

    @Test
    public void doFilterInternal_givenValidToken_shouldAuthenticateUser()
            throws ServletException, IOException {

        // ARRANGE

        when(httpServletRequest.getHeader("Authorization"))
                .thenReturn("Bearer " + TOKEN);

        when(jwtService.extractUsername(TOKEN))
                .thenReturn(USERNAME);

        when(userDetailsService.loadUserByUsername(USERNAME))
                .thenReturn(userDetails);

        // ACT

        jwtAuthenticationFilter.doFilter(
                httpServletRequest,
                httpServletResponse,
                filterChain
        );

        // ASSERT

        verify(jwtService).extractUsername(TOKEN);

        verify(userDetailsService).loadUserByUsername(USERNAME);

        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_" + USER_ROLE);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "not-a-bearer-token")
    public void doFilterInternal_givenInvalidAuthorizationHeader_shouldContinueChain(
            String authorizationHeader
    ) throws ServletException, IOException {

        // ARRANGE

        when(httpServletRequest.getHeader("Authorization"))
                .thenReturn(authorizationHeader);

        // ACT

        jwtAuthenticationFilter.doFilter(
                httpServletRequest,
                httpServletResponse,
                filterChain
        );

        // ASSERT

        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);

        verify(jwtService, never()).extractUsername(any(String.class));
        verify(userDetailsService, never()).loadUserByUsername(any(String.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    public void doFilterInternal_givenInvalidToken_shouldContinueChain()
            throws ServletException, IOException {

        // ARRANGE

        when(httpServletRequest.getHeader("Authorization"))
                .thenReturn("Bearer " + TOKEN);

        when(jwtService.extractUsername(TOKEN))
                .thenThrow(new JwtException("Invalid token"));

        // ACT

        jwtAuthenticationFilter.doFilter(
                httpServletRequest,
                httpServletResponse,
                filterChain
        );

        // ASSERT

        verify(jwtService).extractUsername(TOKEN);

        verify(userDetailsService, never()).loadUserByUsername(any(String.class));

        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    public void doFilterInternal_givenExistingAuthentication_shouldNotOverwriteAuthentication()
            throws ServletException, IOException {

        // ARRANGE

        Authentication existingAuthentication = mock(Authentication.class);

        SecurityContextHolder.getContext().setAuthentication(existingAuthentication);

        when(httpServletRequest.getHeader("Authorization"))
                .thenReturn("Bearer " + TOKEN);

        when(jwtService.extractUsername(TOKEN))
                .thenReturn(USERNAME);

        // ACT

        jwtAuthenticationFilter.doFilter(
                httpServletRequest,
                httpServletResponse,
                filterChain
        );

        // ASSERT

        verify(jwtService).extractUsername(TOKEN);
        verify(userDetailsService, never()).loadUserByUsername(any(String.class));

        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isSameAs(existingAuthentication);
    }

    // ---

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .roles(USER_ROLE)
                .build();
    }
}
