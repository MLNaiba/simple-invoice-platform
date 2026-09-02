package io.github.MLNaiba.simpleinvoice.dto;

import io.github.MLNaiba.simpleinvoice.domain.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceResponse(
        String id,
        String customerId,
        String customerName,
        LocalDateTime issueDate,
        InvoiceStatus status,
        List<InvoiceLineResponse> invoiceLines,
        BigDecimal totalAmount
) {
}
