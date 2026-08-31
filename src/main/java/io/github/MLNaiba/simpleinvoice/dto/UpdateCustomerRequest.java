package io.github.MLNaiba.simpleinvoice.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCustomerRequest(
        @NotBlank(message = "Customer name must not be blank")
        String name
) {
}
