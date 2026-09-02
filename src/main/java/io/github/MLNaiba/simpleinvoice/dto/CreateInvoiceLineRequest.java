package io.github.MLNaiba.simpleinvoice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateInvoiceLineRequest(
        @NotBlank(message = "Product ID must not be blank")
        String productId,

        @Positive(message = "Product quantity must be positive")
        int quantity
) {
}
