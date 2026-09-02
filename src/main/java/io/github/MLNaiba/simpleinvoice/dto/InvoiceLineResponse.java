package io.github.MLNaiba.simpleinvoice.dto;

import java.math.BigDecimal;

public record InvoiceLineResponse(
        String productId,
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
}
