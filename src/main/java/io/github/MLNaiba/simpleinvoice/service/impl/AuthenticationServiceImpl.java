package io.github.MLNaiba.simpleinvoice.service.impl;

import io.github.MLNaiba.simpleinvoice.dto.AuthenticationRequest;
import io.github.MLNaiba.simpleinvoice.dto.AuthenticationResponse;
import io.github.MLNaiba.simpleinvoice.service.AuthenticationService;
import io.github.MLNaiba.simpleinvoice.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info(
                "Authenticating user [{}]",
                request.username()
        );

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        String token = jwtService.generateToken(
                (UserDetails) authentication.getPrincipal());

        log.info(
                "User [{}] authenticated successfully",
                request.username()
        );

        return new AuthenticationResponse(token);
    }
}
