package io.github.MLNaiba.simpleinvoice.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.jwt")
@Validated
public record JwtProperties(
        @NotBlank(message = "JWT secret must not be blank")
        String secret,

        @NotNull(message = "Expiration must not be null")
        Duration expiration
) {
}
