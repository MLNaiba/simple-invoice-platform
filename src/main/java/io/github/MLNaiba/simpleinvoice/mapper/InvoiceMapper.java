package io.github.MLNaiba.simpleinvoice.mapper;

import io.github.MLNaiba.simpleinvoice.domain.Invoice;
import io.github.MLNaiba.simpleinvoice.domain.InvoiceLine;
import io.github.MLNaiba.simpleinvoice.dto.InvoiceLineResponse;
import io.github.MLNaiba.simpleinvoice.dto.InvoiceResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvoiceMapper {
    public InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getCustomerId(),
                invoice.getCustomerName(),
                invoice.getIssueDate(),
                invoice.getStatus(),
                toInvoiceLineResponses(invoice.getInvoiceLines()),
                invoice.getTotalAmount()
        );
    }

    // ---

    private List<InvoiceLineResponse> toInvoiceLineResponses(
            List<InvoiceLine> invoiceLines
    ) {
        return invoiceLines.stream()
                .map(invoiceLine -> new InvoiceLineResponse(
                        invoiceLine.getProductId(),
                        invoiceLine.getProductName(),
                        invoiceLine.getQuantity(),
                        invoiceLine.getUnitPrice()
                ))
                .toList();
    }
}
