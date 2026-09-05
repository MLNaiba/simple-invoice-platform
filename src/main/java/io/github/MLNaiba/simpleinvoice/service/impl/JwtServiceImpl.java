package io.github.MLNaiba.simpleinvoice.service.impl;

import io.github.MLNaiba.simpleinvoice.security.JwtProperties;
import io.github.MLNaiba.simpleinvoice.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {

    private final JwtProperties jwtProperties;

    @Override
    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.expiration());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // ---

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtProperties.secret()));
    }
}
