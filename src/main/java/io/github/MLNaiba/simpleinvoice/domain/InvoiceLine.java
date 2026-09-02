package io.github.MLNaiba.simpleinvoice.domain;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLine {

    private String productId;

    private String productName;

    private int quantity;

    private BigDecimal unitPrice;
}
