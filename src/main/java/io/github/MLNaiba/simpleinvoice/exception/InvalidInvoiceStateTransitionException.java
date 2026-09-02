package io.github.MLNaiba.simpleinvoice.exception;

import io.github.MLNaiba.simpleinvoice.domain.InvoiceStatus;

public class InvalidInvoiceStateTransitionException extends RuntimeException {
    public InvalidInvoiceStateTransitionException(
            InvoiceStatus from,
            InvoiceStatus to
    ) {
        super("Cannot change invoice status from %s to %s".formatted(from, to));
    }
}
