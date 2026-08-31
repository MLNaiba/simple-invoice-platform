package io.github.MLNaiba.simpleinvoice.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(
        @NotBlank(message = "Customer name must not be blank")
        String name
) {
}
