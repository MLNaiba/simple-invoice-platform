package io.github.MLNaiba.simpleinvoice.service;

import io.github.MLNaiba.simpleinvoice.domain.*;
import io.github.MLNaiba.simpleinvoice.dto.*;
import io.github.MLNaiba.simpleinvoice.exception.InvalidInvoiceStateTransitionException;
import io.github.MLNaiba.simpleinvoice.exception.ResourceNotFoundException;
import io.github.MLNaiba.simpleinvoice.mapper.InvoiceMapper;
import io.github.MLNaiba.simpleinvoice.repository.CustomerRepository;
import io.github.MLNaiba.simpleinvoice.repository.InvoiceRepository;
import io.github.MLNaiba.simpleinvoice.repository.ProductRepository;
import io.github.MLNaiba.simpleinvoice.service.impl.InvoiceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvoiceServiceTests {

    private static final String ID = "id";

    private static final String CUSTOMER_ID = "customer-id";
    private static final String CUSTOMER_NAME = "customer-name";

    private static final LocalDateTime ISSUE_TIME = LocalDateTime.now();

    private static final String PRODUCT_ID = "product-id";
    private static final String PRODUCT_NAME = "product-name";
    private static final int PRODUCT_QUANTITY = 5;
    private static final BigDecimal PRODUCT_UNIT_PRICE = BigDecimal.valueOf(10);

    private static final String INVALID_ID = "invalid-id";
    private static final String INVALID_CUSTOMER_ID = "invalid-customer-id";
    private static final String INVALID_PRODUCT_ID = "invalid-product-id";

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Captor
    private ArgumentCaptor<Invoice> invoiceCaptor;

    @ParameterizedTest
    @ValueSource(ints = {1, 3})
    public void createInvoice_givenValidRequest_shouldReturnCreatedInvoice(
            int count
    ) {

        // ARRANGE

        LocalDateTime before = LocalDateTime.now();

        Customer customer = customer(count);

        List<Product> products =
                IntStream.range(0, count)
                        .mapToObj(this::product)
                        .toList();

        Invoice invoice = invoice(count);

        when(customerRepository.findById(customer.getId()))
                .thenReturn(Optional.of(customer));

        for (Product product : products) {
            when(productRepository.findById(product.getId()))
                    .thenReturn(Optional.of(product));
        }

        when(invoiceRepository.save(any(Invoice.class)))
                .thenReturn(invoice);

        // ACT

        InvoiceResponse response =
                invoiceService.createInvoice(createInvoiceRequest(invoice));

        // ASSERT

        verify(customerRepository).findById(customer.getId());
        for (Product product : products) {
            verify(productRepository).findById(product.getId());
        }

        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice capturedInvoice = invoiceCaptor.getValue();

        assertThat(capturedInvoice)
                .usingRecursiveComparison()
                .ignoringFields("id", "issueDate")
                .isEqualTo(invoice);

        assertThat(capturedInvoice.getIssueDate())
                .isBetween(before, LocalDateTime.now());

        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(invoice);

        verify(invoiceMapper).toResponse(invoice);
    }

    @Test
    public void createInvoice_givenInvalidCustomerId_shouldThrowResourceNotFoundException() {

        // ARRANGE

        Invoice invoice = invoice();
        invoice.setCustomerId(INVALID_CUSTOMER_ID);

        when(customerRepository.findById(INVALID_CUSTOMER_ID))
                .thenReturn(Optional.empty());

        // ACT / ASSERT

        assertThatThrownBy(() ->
                invoiceService.createInvoice(createInvoiceRequest(invoice)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(INVALID_CUSTOMER_ID);

        verify(customerRepository).findById(INVALID_CUSTOMER_ID);

        verify(productRepository, never()).findById(any(String.class));

        verify(invoiceRepository, never()).save(any(Invoice.class));

        verify(invoiceMapper, never()).toResponse(any());
    }

    @Test
    public void createInvoice_givenInvalidProductId_shouldThrowResourceNotFoundException() {

        Invoice invoice = invoice();
        invoice.getInvoiceLines().get(0).setProductId(INVALID_PRODUCT_ID);

        Customer customer = customer();

        when(customerRepository.findById(CUSTOMER_ID))
                .thenReturn(Optional.of(customer));

        when(productRepository.findById(INVALID_PRODUCT_ID))
                .thenReturn(Optional.empty());

        // ACT / ASSERT

        assertThatThrownBy(() ->
                invoiceService.createInvoice(createInvoiceRequest(invoice)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(INVALID_PRODUCT_ID);

        verify(customerRepository).findById(CUSTOMER_ID);

        verify(productRepository).findById(INVALID_PRODUCT_ID);

        verify(invoiceRepository, never()).save(any(Invoice.class));

        verify(invoiceMapper, never()).toResponse(any());
    }

    @Test
    public void getInvoiceById_givenValidId_shouldReturnInvoice() {

        // ARRANGE

        Invoice invoice = invoice();

        when(invoiceRepository.findById(invoice.getId()))
                .thenReturn(Optional.of(invoice));

        // ACT

        InvoiceResponse response = invoiceService.getInvoiceById(invoice.getId());

        // ASSERT

        verify(invoiceRepository).findById(invoice.getId());

        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(invoice);

        verify(invoiceMapper).toResponse(invoice);
    }

    @Test
    public void getInvoiceById_givenInvalidId_shouldThrowResourceNotFoundException() {

        // ARRANGE

        when(invoiceRepository.findById(INVALID_ID))
                .thenReturn(Optional.empty());

        // ACT / ASSERT

        assertThatThrownBy(() ->
                invoiceService.getInvoiceById(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(INVALID_ID);

        verify(invoiceRepository).findById(INVALID_ID);

        verify(invoiceMapper, never()).toResponse(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 0})
    public void getAllInvoices_givenMultipleInvoices_shouldReturnInvoiceList(
            int invoiceCount
    ) {

        // ARRANGE

        List<Invoice> invoices = IntStream.range(0, invoiceCount)
                .mapToObj(this::invoice)
                .toList();

        when(invoiceRepository.findAll()).thenReturn(invoices);

        // ACT

        List<InvoiceResponse> responses = invoiceService.getAllInvoices();

        // ASSERT

        verify(invoiceRepository).findAll();

        assertThat(responses).hasSize(invoices.size());

        for (int i = 0; i < responses.size(); ++i) {
            assertThat(responses.get(i))
                    .usingRecursiveComparison()
                    .isEqualTo(invoices.get(i));
        }

        for (Invoice invoice : invoices) {
            verify(invoiceMapper).toResponse(invoice);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "CREATED, VALIDATED",
            "VALIDATED, SENT",
            "SENT, PAID",
            "CREATED, CANCELED",
            "VALIDATED, CANCELED",
            "SENT, CANCELED"
    })
    public void updateInvoice_givenValidRequest_shouldReturnUpdatedInvoice(
            InvoiceStatus statusFrom,
            InvoiceStatus statusTo
    ) {

        // ARRANGE

        Invoice invoice = invoice();
        invoice.setStatus(statusFrom);

        when(invoiceRepository.findById(ID))
                .thenReturn(Optional.of(invoice));

        UpdateInvoiceRequest request = new UpdateInvoiceRequest(statusTo);

        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT

        InvoiceResponse response = invoiceService.updateInvoice(invoice.getId(), request);

        // ASSERT

        verify(invoiceRepository).findById(ID);

        assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields("status")
                .isEqualTo(invoice);

        assertThat(response.status()).isEqualTo(statusTo);

        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice captured = invoiceCaptor.getValue();
        assertThat(captured.getStatus()).isEqualTo(statusTo);

        verify(invoiceMapper).toResponse(invoice);
    }

    @ParameterizedTest
    @CsvSource({
            "CREATED, PAID",
            "PAID, CREATED",
            "CANCELED, VALIDATED"
    })
    public void updateInvoice_givenInvalidStateTransition_shouldThrowInvalidInvoiceStateTransitionException(
            InvoiceStatus statusFrom,
            InvoiceStatus statusTo
    ) {

        // ARRANGE

        Invoice invoice = invoice();
        invoice.setStatus(statusFrom);

        when(invoiceRepository.findById(ID)).thenReturn(Optional.of(invoice));

        UpdateInvoiceRequest request = new UpdateInvoiceRequest(statusTo);

        // ACT / ASSERT

        assertThatThrownBy(() ->
                invoiceService.updateInvoice(invoice.getId(), request))
                .isInstanceOf(InvalidInvoiceStateTransitionException.class)
                .hasMessageContainingAll(statusFrom.toString(), statusTo.toString());

        verify(invoiceRepository).findById(ID);

        verify(invoiceRepository, never()).save(any(Invoice.class));
        verify(invoiceMapper, never()).toResponse(any(Invoice.class));
    }

    @Test
    public void updateInvoice_givenInvalidId_shouldThrowResourceNotFoundException() {

        // ARRANGE

        when(invoiceRepository.findById(INVALID_ID))
                .thenReturn(Optional.empty());

        // ACT / ASSERT

        assertThatThrownBy(() ->
                invoiceService.updateInvoice(
                        INVALID_ID,
                        new UpdateInvoiceRequest(InvoiceStatus.CANCELED)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(INVALID_ID);

        verify(invoiceRepository).findById(INVALID_ID);

        verify(invoiceRepository, never()).save(any(Invoice.class));

        verify(invoiceMapper, never()).toResponse(any());
    }

    @Test
    public void deleteInvoice_givenValidId_shouldDeleteInvoice() {

        // ARRANGE

        Invoice invoice = invoice();

        when(invoiceRepository.findById(invoice.getId()))
                .thenReturn(Optional.of(invoice));

        // ACT

        invoiceService.deleteInvoice(invoice.getId());

        // ASSERT

        verify(invoiceRepository).findById(invoice.getId());
        verify(invoiceRepository).delete(invoice);

        verify(invoiceMapper, never()).toResponse(any());
    }

    @Test
    public void deleteInvoice_givenInvalidId_shouldThrowResourceNotFoundException() {

        // ARRANGE

        when(invoiceRepository.findById(INVALID_ID))
                .thenReturn(Optional.empty());

        // ACT / ASSERT

        assertThatThrownBy(() ->
                invoiceService.deleteInvoice(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(INVALID_ID);

        verify(invoiceRepository).findById(INVALID_ID);

        verify(invoiceRepository, never()).delete(any(Invoice.class));

        verify(invoiceMapper, never()).toResponse(any());
    }

    // ---

    @BeforeEach
    public void setUp() {
        lenient().when(invoiceMapper.toResponse(any(Invoice.class)))
                .thenAnswer(invocation -> {
                    Invoice invoice = invocation.getArgument(0);

                    return new InvoiceResponse(
                            invoice.getId(),
                            invoice.getCustomerId(),
                            invoice.getCustomerName(),
                            invoice.getIssueDate(),
                            invoice.getStatus(),
                            invoiceLineResponses(invoice.getInvoiceLines()),
                            invoice.getTotalAmount()
                    );
                });
    }

    // ---

    private Invoice invoice(int num) {
        List<InvoiceLine> invoiceLines =
                IntStream.range(0, num)
                        .mapToObj(this::invoiceLine)
                        .toList();

        return Invoice.builder()
                .id(ID + num)
                .customerId(CUSTOMER_ID + num)
                .customerName(CUSTOMER_NAME + num)
                .issueDate(ISSUE_TIME)
                .status(InvoiceStatus.CREATED)
                .invoiceLines(invoiceLines)
                .totalAmount(totalAmount(invoiceLines))
                .build();
    }

    private Invoice invoice() {
        List<InvoiceLine> invoiceLines = List.of(invoiceLine());

        return Invoice.builder()
                .id(ID)
                .customerId(CUSTOMER_ID)
                .customerName(CUSTOMER_NAME)
                .issueDate(ISSUE_TIME)
                .status(InvoiceStatus.CREATED)
                .invoiceLines(invoiceLines)
                .totalAmount(totalAmount(invoiceLines))
                .build();
    }

    private InvoiceLine invoiceLine(int num) {
        return InvoiceLine.builder()
                .productId(PRODUCT_ID + num)
                .productName(PRODUCT_NAME + num)
                .quantity(PRODUCT_QUANTITY)
                .unitPrice(PRODUCT_UNIT_PRICE.add(BigDecimal.valueOf(num)))
                .build();
    }

    private InvoiceLine invoiceLine() {
        return InvoiceLine.builder()
                .productId(PRODUCT_ID)
                .productName(PRODUCT_NAME)
                .quantity(PRODUCT_QUANTITY)
                .unitPrice(PRODUCT_UNIT_PRICE)
                .build();
    }

    private Customer customer(int num) {
        return Customer.builder()
                .id(CUSTOMER_ID + num)
                .name(CUSTOMER_NAME + num)
                .build();
    }

    private Customer customer() {
        return Customer.builder()
                .id(CUSTOMER_ID)
                .name(CUSTOMER_NAME)
                .build();
    }

    private Product product(int num) {
        return Product.builder()
                .id(PRODUCT_ID + num)
                .name(PRODUCT_NAME + num)
                .price(PRODUCT_UNIT_PRICE.add(BigDecimal.valueOf(num)))
                .build();
    }

    private CreateInvoiceRequest createInvoiceRequest(Invoice invoice) {
        return new CreateInvoiceRequest(
                invoice.getCustomerId(),
                invoice.getInvoiceLines().stream()
                        .map(invoiceLine -> new CreateInvoiceLineRequest(
                                invoiceLine.getProductId(),
                                invoiceLine.getQuantity())
                        )
                        .toList()
        );
    }

    private List<InvoiceLineResponse> invoiceLineResponses(List<InvoiceLine> invoiceLines) {
        return invoiceLines.stream()
                .map(invoiceLine -> new InvoiceLineResponse(
                        invoiceLine.getProductId(),
                        invoiceLine.getProductName(),
                        invoiceLine.getQuantity(),
                        invoiceLine.getUnitPrice()
                ))
                .toList();
    }

    private BigDecimal totalAmount(List<InvoiceLine> invoiceLines) {
        return invoiceLines.stream()
                .map(line -> line.getUnitPrice()
                        .multiply(BigDecimal.valueOf(line.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
