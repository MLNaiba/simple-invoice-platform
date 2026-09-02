package io.github.MLNaiba.simpleinvoice.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("Invoice")
public class Invoice {
    @Id
    private String id;

    private String customerId;

    private String customerName;

    private LocalDateTime issueDate;

    private InvoiceStatus status;

    private List<InvoiceLine> invoiceLines;

    private BigDecimal totalAmount;
}
