package io.github.MLNaiba.simpleinvoice.service.impl;

import io.github.MLNaiba.simpleinvoice.domain.*;
import io.github.MLNaiba.simpleinvoice.dto.CreateInvoiceLineRequest;
import io.github.MLNaiba.simpleinvoice.dto.CreateInvoiceRequest;
import io.github.MLNaiba.simpleinvoice.dto.InvoiceResponse;
import io.github.MLNaiba.simpleinvoice.dto.UpdateInvoiceRequest;
import io.github.MLNaiba.simpleinvoice.exception.InvalidInvoiceStateTransitionException;
import io.github.MLNaiba.simpleinvoice.exception.ResourceNotFoundException;
import io.github.MLNaiba.simpleinvoice.mapper.InvoiceMapper;
import io.github.MLNaiba.simpleinvoice.repository.CustomerRepository;
import io.github.MLNaiba.simpleinvoice.repository.InvoiceRepository;
import io.github.MLNaiba.simpleinvoice.repository.ProductRepository;
import io.github.MLNaiba.simpleinvoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static io.github.MLNaiba.simpleinvoice.domain.InvoiceStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Override
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        log.info(
                "Creating invoice for customer id [{}]",
                request.customerId()
        );

        Customer customer = findCustomerById(request.customerId());

        List<InvoiceLine> invoiceLines = request.invoiceLines().stream()
                .map(this::createInvoiceLine)
                .toList();

        Invoice invoice = Invoice.builder()
                .customerId(customer.getId())
                .customerName(customer.getName())
                .issueDate(LocalDateTime.now())
                .status(CREATED)
                .invoiceLines(invoiceLines)
                .totalAmount(getTotalAmount(invoiceLines))
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        log.info(
                "Invoice for customer [{}] with id [{}] successfully created",
                customer.getName(),
                customer.getId()
        );

        return invoiceMapper.toResponse(savedInvoice);
    }

    @Override
    public InvoiceResponse getInvoiceById(String id) {
        log.info(
                "Retrieving invoice with id [{}]",
                id
        );

        Invoice invoice = findById(id);

        log.info(
                "Invoice with id [{}] successfully retrieved",
                id
        );

        return invoiceMapper.toResponse(invoice);
    }

    @Override
    public List<InvoiceResponse> getAllInvoices() {
        log.info(
                "Retrieving all invoices"
        );

        List<Invoice> invoices = invoiceRepository.findAll();

        log.info(
                "All invoices successfully retrieved"
        );

        return invoices.stream()
                .map(invoiceMapper::toResponse)
                .toList();
    }

    @Override
    public InvoiceResponse updateInvoice(String id, UpdateInvoiceRequest request) {
        log.info(
                "Updating invoice with id [{}]",
                id
        );

        Invoice invoice = findById(id);

        if (!isValidStateTransition(invoice.getStatus(), request.status())) {
            throw new InvalidInvoiceStateTransitionException(
                    invoice.getStatus(),
                    request.status()
            );
        }

        invoice.setStatus(request.status());

        Invoice savedInvoice = invoiceRepository.save(invoice);

        log.info(
                "Invoice with id [{}] successfully updated",
                id
        );

        return invoiceMapper.toResponse(savedInvoice);
    }

    @Override
    public void deleteInvoice(String id) {
        log.info(
                "Deleting invoice with id [{}]",
                id
        );

        Invoice invoice = findById(id);

        invoiceRepository.delete(invoice);

        log.info(
                "Invoice with id [{}] successfully deleted",
                id
        );
    }

    // ---

    private Invoice findById(String id)
            throws ResourceNotFoundException {
        return invoiceRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }

    private Customer findCustomerById(String id)
            throws ResourceNotFoundException {
        return customerRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("customer", id));
    }

    private Product findProductById(String id)
            throws ResourceNotFoundException {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("product", id));
    }

    private InvoiceLine createInvoiceLine(CreateInvoiceLineRequest request) {
        Product product = findProductById(request.productId());

        return InvoiceLine.builder()
                .productId(product.getId())
                .productName(product.getName())
                .quantity(request.quantity())
                .unitPrice(product.getPrice())
                .build();
    }

    private boolean isValidStateTransition(
            InvoiceStatus from,
            InvoiceStatus to
    ) {
        return switch (from) {
            case CREATED -> to == VALIDATED || to == CANCELED;
            case VALIDATED -> to == SENT || to == CANCELED;
            case SENT -> to == PAID || to == CANCELED;
            default -> false;
        };
    }

    private BigDecimal getTotalAmount(List<InvoiceLine> invoiceLines) {
        return invoiceLines.stream()
                .map(line -> line.getUnitPrice()
                        .multiply(BigDecimal.valueOf(line.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
