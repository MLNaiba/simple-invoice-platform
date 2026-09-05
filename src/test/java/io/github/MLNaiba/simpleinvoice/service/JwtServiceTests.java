package io.github.MLNaiba.simpleinvoice.service;

import io.github.MLNaiba.simpleinvoice.security.JwtProperties;
import io.github.MLNaiba.simpleinvoice.service.impl.JwtServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JwtServiceTests {

    private static final String SECRET = "sTa8+GFuWIuC4fZcukD8crUE6P9/qiUdckfZFmnlq0Q=";
    private static final Duration EXPIRATION = Duration.ofHours(1);

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String USER_ROLE = "USER";

    private UserDetails userDetails;

    private JwtServiceImpl jwtService;

    @Test
    public void generateToken_givenValidUserDetails_shouldReturnToken() {

        // ARRANGE / ACT

        String token = jwtService.generateToken(userDetails);

        // ASSERT

        assertThat(token).isNotBlank();
    }

    @Test
    public void extractUsername_givenValidToken_shouldReturnUsername() {

        // ARRANGE

        String token = jwtService.generateToken(userDetails);

        // ACT

        String username = jwtService.extractUsername(token);

        // ASSERT

        assertThat(username).isEqualTo(userDetails.getUsername());
    }

    @Test
    public void extractUsername_givenTamperedToken_shouldThrowJwtException() {

        // ARRANGE

        String token = jwtService.generateToken(userDetails);

        String tamperedToken =
                token.substring(0, token.length() - 1) + "x";

        // ACT / ASSERT

        assertThatThrownBy(() ->
                jwtService.extractUsername(tamperedToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    public void extractUsername_givenExpiredToken_shouldThrowExpiredJwtException() {

        // ARRANGE

        String token = expiredToken();

        // ACT / ASSERT

        assertThatThrownBy(() ->
                jwtService.extractUsername(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    // ---

    @BeforeEach
    public void setUp() {
        JwtProperties jwtProperties = new JwtProperties(SECRET, EXPIRATION);

        jwtService = new JwtServiceImpl(jwtProperties);

        userDetails = org.springframework.security.core.userdetails.User
                .withUsername(USERNAME)
                .password(PASSWORD)
                .roles(USER_ROLE)
                .build();
    }

    // ---

    private SecretKey getTestSigningKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(SECRET)
        );
    }

    private String expiredToken() {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now.minus(Duration.ofHours(2))))
                .expiration(Date.from(now.minus(Duration.ofHours(1))))
                .signWith(getTestSigningKey())
                .compact();
    }
}
