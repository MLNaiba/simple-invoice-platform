package io.github.MLNaiba.simpleinvoice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank(message = "Product name must not be blank")
        String name,

        @NotNull(message = "Product price must not be null")
        @Positive(message = "Product price must be positive")
        BigDecimal price
) {
}
