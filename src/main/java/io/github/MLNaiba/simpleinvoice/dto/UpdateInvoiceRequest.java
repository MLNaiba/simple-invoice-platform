package io.github.MLNaiba.simpleinvoice.dto;

import io.github.MLNaiba.simpleinvoice.domain.InvoiceStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateInvoiceRequest(
        @NotNull(message = "Invoice status must not be null")
        InvoiceStatus status
) {
}
