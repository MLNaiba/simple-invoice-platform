package io.github.MLNaiba.simpleinvoice.service;

import io.github.MLNaiba.simpleinvoice.dto.CreateInvoiceRequest;
import io.github.MLNaiba.simpleinvoice.dto.InvoiceResponse;
import io.github.MLNaiba.simpleinvoice.dto.UpdateInvoiceRequest;

import java.util.List;

public interface InvoiceService {

    InvoiceResponse createInvoice(CreateInvoiceRequest request);

    InvoiceResponse getInvoiceById(String id);

    List<InvoiceResponse> getAllInvoices();

    InvoiceResponse updateInvoice(String id, UpdateInvoiceRequest request);

    void deleteInvoice(String id);
}
