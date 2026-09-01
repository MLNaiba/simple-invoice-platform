package io.github.MLNaiba.simpleinvoice.dto;

import java.math.BigDecimal;

public record ProductResponse(
        String id,
        String name,
        BigDecimal price
) {
}
