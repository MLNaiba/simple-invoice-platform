package io.github.MLNaiba.simpleinvoice.controller;

import io.github.MLNaiba.simpleinvoice.dto.CreateInvoiceRequest;
import io.github.MLNaiba.simpleinvoice.dto.InvoiceResponse;
import io.github.MLNaiba.simpleinvoice.dto.UpdateInvoiceRequest;
import io.github.MLNaiba.simpleinvoice.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request) {
        return invoiceService.createInvoice(request);
    }

    @GetMapping("/{id}")
    public InvoiceResponse getInvoiceById(@PathVariable String id) {
        return invoiceService.getInvoiceById(id);
    }

    @GetMapping
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    @PutMapping("/{id}")
    public InvoiceResponse updateInvoiceStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateInvoiceRequest request) {
        return invoiceService.updateInvoice(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInvoice(@PathVariable String id) {
        invoiceService.deleteInvoice(id);
    }
}
