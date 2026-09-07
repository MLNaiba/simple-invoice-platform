package io.github.MLNaiba.simpleinvoice.controller;

import io.github.MLNaiba.simpleinvoice.domain.InvoiceStatus;
import io.github.MLNaiba.simpleinvoice.dto.*;
import io.github.MLNaiba.simpleinvoice.exception.ResourceNotFoundException;
import io.github.MLNaiba.simpleinvoice.service.InvoiceService;
import io.github.MLNaiba.simpleinvoice.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvoiceController.class)
@AutoConfigureMockMvc(addFilters = false)
public class InvoiceControllerTests {

    private static final String ID = "id";

    private static final String CUSTOMER_ID = "customer-id";
    private static final String CUSTOMER_NAME = "customer-name";

    private static final LocalDateTime ISSUE_TIME = LocalDateTime.now();

    private static final String PRODUCT_ID = "product-id";
    private static final String PRODUCT_NAME = "product-name";
    private static final int PRODUCT_QUANTITY = 5;
    private static final BigDecimal PRODUCT_UNIT_PRICE = BigDecimal.valueOf(10);

    private static final String INVALID_ID = "invalid-id";
    private static final String BLANK_CUSTOMER_ID = "";
    private static final String BLANK_PRODUCT_ID = "";
    private static final int INVALID_PRODUCT_QUANTITY = -1;

    private static final String BASE_URL = "/api/invoices";
    private static final String ID_URL = BASE_URL + "/%s";

    private static final String VALIDATION_FAILED = "Validation failed";
    private static final String CUSTOMER_ID_REQUIRED = "customerId: Customer ID must not be blank";
    private static final String INVOICE_LINES_REQUIRED = "invoiceLines: At least one invoice line must be present";
    private static final String PRODUCT_ID_REQUIRED = "invoiceLines[%s].productId: Product ID must not be blank";
    private static final String PRODUCT_QUANTITY_MUST_BE_POSITIVE = "invoiceLines[%s].quantity: Product quantity must be positive";
    private static final String INVOICE_STATUS_NOT_NULL = "status: Invoice status must not be null";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InvoiceService invoiceService;

    // Security filter dependencies required by @WebMvcTest
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @ParameterizedTest
    @ValueSource(ints = {1, 3})
    public void createInvoice_givenValidRequest_shouldReturnCreatedInvoice(
            int invoiceLineCount
    ) throws Exception {

        // ARRANGE

        CreateInvoiceRequest createInvoiceRequest = createRequest(invoiceLineCount);

        InvoiceResponse invoiceResponse = response(invoiceLineCount);

        given(invoiceService.createInvoice(createInvoiceRequest))
                .willReturn(invoiceResponse);

        // ACT

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createInvoiceRequest)));

        // ASSERT

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(invoiceResponse.id()))
                .andExpect(jsonPath("$.customerId").value(invoiceResponse.customerId()))
                .andExpect(jsonPath("$.customerName").value(invoiceResponse.customerName()))
                .andExpect(jsonPath("$.issueDate").exists())
                .andExpect(jsonPath("$.status").value(invoiceResponse.status().name()))
                .andExpect(jsonPath("$.totalAmount").value(invoiceResponse.totalAmount().doubleValue()))
                .andExpect(jsonPath("$.invoiceLines").isArray())
                .andExpect(jsonPath("$.invoiceLines.length()").value(invoiceLineCount));

        for (int i = 0; i < invoiceLineCount; ++i) {
            List<InvoiceLineResponse> invoiceLineResponses = invoiceResponse.invoiceLines();

            response.andExpect(
                            jsonPath("$.invoiceLines[%s].productId".formatted(i))
                                    .value(invoiceLineResponses.get(i).productId()))
                    .andExpect(
                            jsonPath("$.invoiceLines[%s].productName".formatted(i))
                                    .value(invoiceLineResponses.get(i).productName()))
                    .andExpect(
                            jsonPath("$.invoiceLines[%s].quantity".formatted(i))
                                    .value(invoiceLineResponses.get(i).quantity()))
                    .andExpect(
                            jsonPath("$.invoiceLines[%s].unitPrice".formatted(i))
                                    .value(invoiceLineResponses.get(i).unitPrice().doubleValue()));
        }

        verify(invoiceService).createInvoice(createInvoiceRequest);
    }

    @ParameterizedTest
    @MethodSource("invalidInvoiceData")
    public void createInvoice_givenInvalidRequestBadParameters_shouldReturnBadRequest(
            String name,
            List<CreateInvoiceLineRequest> invoiceLineRequestList,
            List<String> expectedErrors
    ) throws Exception {

        // ARRANGE

        CreateInvoiceRequest createInvoiceRequest =
                new CreateInvoiceRequest(name, invoiceLineRequestList);

        // ACT

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createInvoiceRequest)));

        // ASSERT

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(VALIDATION_FAILED))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(expectedErrors.size())));

        for (String expectedError : expectedErrors) {
            response.andExpect(jsonPath("$.errors", hasItem(expectedError)));
        }

        verify(invoiceService, never()).createInvoice(any());
    }

    @Test
    public void getInvoiceById_givenValidRequest_shouldReturnInvoice() throws Exception {

        // ARRANGE

        InvoiceResponse invoiceResponse = response();

        given(invoiceService.getInvoiceById(invoiceResponse.id()))
                .willReturn(invoiceResponse);

        // ACT

        ResultActions response = mockMvc.perform(get(ID_URL.formatted(invoiceResponse.id())));

        // ASSERT

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoiceResponse.id()))
                .andExpect(jsonPath("$.customerId").value(invoiceResponse.customerId()))
                .andExpect(jsonPath("$.customerName").value(invoiceResponse.customerName()))
                .andExpect(jsonPath("$.issueDate").exists())
                .andExpect(jsonPath("$.status").value(invoiceResponse.status().name()))
                .andExpect(jsonPath("$.totalAmount").value(invoiceResponse.totalAmount().doubleValue()))
                .andExpect(jsonPath("$.invoiceLines").isArray())
                .andExpect(jsonPath("$.invoiceLines.length()").value(invoiceResponse.invoiceLines().size()));

        for (int i = 0; i < invoiceResponse.invoiceLines().size(); ++i) {
            InvoiceLineResponse lineResponse = invoiceResponse.invoiceLines().get(i);

            response.andExpect(
                            jsonPath("$.invoiceLines[%s].productId".formatted(i))
                                    .value(lineResponse.productId()))
                    .andExpect(
                            jsonPath("$.invoiceLines[%s].productName".formatted(i))
                                    .value(lineResponse.productName()))
                    .andExpect(
                            jsonPath("$.invoiceLines[%s].quantity".formatted(i))
                                    .value(lineResponse.quantity()))
                    .andExpect(
                            jsonPath("$.invoiceLines[%s].unitPrice".formatted(i))
                                    .value(lineResponse.unitPrice().doubleValue()));
        }

        verify(invoiceService).getInvoiceById(invoiceResponse.id());
    }

    @Test
    public void getInvoiceById_givenInvalidRequestNonexistentId_shouldReturnNotFound() throws Exception {

        // ARRANGE

        given(invoiceService.getInvoiceById(INVALID_ID))
                .willThrow(new ResourceNotFoundException("Invoice", INVALID_ID));

        // ACT

        ResultActions response = mockMvc.perform(get(ID_URL.formatted(INVALID_ID)));

        // ASSERT

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString(INVALID_ID)))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(invoiceService).getInvoiceById(INVALID_ID);
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 0})
    public void getAllInvoices_givenValidRequest_shouldReturnInvoices(
            int responseCount
    ) throws Exception {

        // ARRANGE

        List<InvoiceResponse> invoiceResponses =
                IntStream.range(0, responseCount)
                        .mapToObj(this::response)
                        .toList();

        given(invoiceService.getAllInvoices())
                .willReturn(invoiceResponses);

        // ACT

        ResultActions response = mockMvc.perform(get(BASE_URL));

        // ASSERT

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(responseCount));

        for (int i = 0; i < responseCount; ++i) {
            InvoiceResponse invoiceResponse = invoiceResponses.get(i);

            response
                    .andExpect(jsonPath("$[%d].id".formatted(i))
                            .value(invoiceResponse.id()))
                    .andExpect(jsonPath("$[%d].customerId".formatted(i))
                            .value(invoiceResponse.customerId()))
                    .andExpect(jsonPath("$[%d].customerName".formatted(i))
                            .value(invoiceResponse.customerName()))
                    .andExpect(jsonPath("$[%d].issueDate".formatted(i))
                            .exists())
                    .andExpect(jsonPath("$[%d].status".formatted(i))
                            .value(invoiceResponse.status().name()))
                    .andExpect(jsonPath("$[%d].totalAmount".formatted(i))
                            .value(invoiceResponse.totalAmount().doubleValue()))
                    .andExpect(jsonPath("$[%d].invoiceLines.length()".formatted(i))
                            .value(invoiceResponse.invoiceLines().size()));

            for (int j = 0; j < invoiceResponse.invoiceLines().size(); ++j) {
                InvoiceLineResponse invoiceLineResponse = invoiceResponse.invoiceLines().get(j);

                response
                        .andExpect(
                                jsonPath("$.[%d].invoiceLines[%d].productId".formatted(i, j))
                                        .value(invoiceLineResponse.productId()))
                        .andExpect(
                                jsonPath("$.[%d].invoiceLines[%d].productName".formatted(i, j))
                                        .value(invoiceLineResponse.productName()))
                        .andExpect(
                                jsonPath("$.[%d].invoiceLines[%d].quantity".formatted(i, j))
                                        .value(invoiceLineResponse.quantity()))
                        .andExpect(
                                jsonPath("$.[%d].invoiceLines[%d].unitPrice".formatted(i, j))
                                        .value(invoiceLineResponse.unitPrice().doubleValue()));
            }
        }

        verify(invoiceService).getAllInvoices();
    }

    @Test
    public void updateInvoice_givenValidRequest_shouldReturnUpdatedInvoice() throws Exception {

        // ARRANGE

        UpdateInvoiceRequest updateInvoiceRequest = updateRequest();

        InvoiceLineResponse lineResponse = lineResponse();

        InvoiceResponse invoiceResponse = new InvoiceResponse(
                ID,
                CUSTOMER_ID,
                CUSTOMER_NAME,
                ISSUE_TIME,
                updateInvoiceRequest.status(),
                List.of(lineResponse),
                lineResponse.unitPrice().multiply(
                        BigDecimal.valueOf(lineResponse.quantity())
                )
        );

        given(invoiceService.updateInvoice(ID, updateInvoiceRequest))
                .willReturn(invoiceResponse);

        // ACT

        ResultActions response = mockMvc.perform(put(ID_URL.formatted(ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateInvoiceRequest)));

        // ASSERT

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoiceResponse.id()))
                .andExpect(jsonPath("$.customerId").value(invoiceResponse.customerId()))
                .andExpect(jsonPath("$.customerName").value(invoiceResponse.customerName()))
                .andExpect(jsonPath("$.issueDate").exists())
                .andExpect(jsonPath("$.status").value(invoiceResponse.status().name()))
                .andExpect(jsonPath("$.totalAmount").value(invoiceResponse.totalAmount().doubleValue()))
                .andExpect(jsonPath("$.invoiceLines").isArray())
                .andExpect(jsonPath("$.invoiceLines.length()").value(invoiceResponse.invoiceLines().size()));

        for (int i = 0; i < invoiceResponse.invoiceLines().size(); ++i) {
            List<InvoiceLineResponse> invoiceLineResponses = invoiceResponse.invoiceLines();

            response.andExpect(
                            jsonPath("$.invoiceLines[%s].productId".formatted(i))
                                    .value(invoiceLineResponses.get(i).productId()))
                    .andExpect(
                            jsonPath("$.invoiceLines[%s].productName".formatted(i))
                                    .value(invoiceLineResponses.get(i).productName()))
                    .andExpect(
                            jsonPath("$.invoiceLines[%s].quantity".formatted(i))
                                    .value(invoiceLineResponses.get(i).quantity()))
                    .andExpect(
                            jsonPath("$.invoiceLines[%s].unitPrice".formatted(i))
                                    .value(invoiceLineResponses.get(i).unitPrice().doubleValue()));
        }

        verify(invoiceService).updateInvoice(
                ID,
                updateInvoiceRequest);
    }

    @Test
    public void updateInvoice_givenInvalidRequestNullStatus_shouldReturnBadRequest() throws Exception {

        // ARRANGE

        UpdateInvoiceRequest updateInvoiceRequest = new UpdateInvoiceRequest(null);

        // ACT

        ResultActions response = mockMvc.perform(put(ID_URL.formatted(ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateInvoiceRequest)));

        // ASSERT

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(VALIDATION_FAILED))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItem(INVOICE_STATUS_NOT_NULL)));

        verify(invoiceService, never()).updateInvoice(
                any(String.class),
                any(UpdateInvoiceRequest.class)
        );
    }

    @Test
    public void updateInvoice_givenInvalidRequestNonexistentId_shouldReturnNotFound() throws Exception {

        // ARRANGE

        UpdateInvoiceRequest updateInvoiceRequest = updateRequest();

        given(invoiceService.updateInvoice(INVALID_ID, updateInvoiceRequest))
                .willThrow(new ResourceNotFoundException("Invoice", INVALID_ID));

        // ACT

        ResultActions response = mockMvc.perform(put(ID_URL.formatted(INVALID_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateInvoiceRequest)));

        // ASSERT

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString(INVALID_ID)))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(invoiceService).updateInvoice(
                INVALID_ID,
                updateInvoiceRequest
        );
    }

    @Test
    public void deleteInvoice_givenValidRequest_shouldDeleteInvoice() throws Exception {

        // ARRANGE / ACT

        ResultActions response = mockMvc.perform(delete(ID_URL.formatted(ID)));

        // ASSERT

        response.andExpect(status().isNoContent());

        verify(invoiceService).deleteInvoice(ID);
    }

    @Test
    public void deleteInvoice_givenInvalidRequestNonexistentId_shouldReturnNotFound() throws Exception {

        // ARRANGE

        doThrow(new ResourceNotFoundException("Invoice", INVALID_ID))
                .when(invoiceService)
                .deleteInvoice(INVALID_ID);

        // ACT

        ResultActions response = mockMvc.perform(delete(ID_URL.formatted(INVALID_ID)));

        // ASSERT

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString(INVALID_ID)))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(invoiceService).deleteInvoice(INVALID_ID);
    }

    // ---

    private static Stream<Arguments> invalidInvoiceData() {
        List<CreateInvoiceLineRequest> validInvoiceLineRequestList =
                List.of(
                        new CreateInvoiceLineRequest(
                                PRODUCT_ID,
                                PRODUCT_QUANTITY
                        )
                );

        List<CreateInvoiceLineRequest> emptyInvoiceLineRequestList = List.of();

        List<CreateInvoiceLineRequest> invoiceLineRequestListWithBlankProductId =
                List.of(
                        new CreateInvoiceLineRequest(
                                BLANK_PRODUCT_ID,
                                PRODUCT_QUANTITY
                        )
                );

        List<CreateInvoiceLineRequest> invoiceLineRequestListWithNonPositiveProductQuantity =
                List.of(
                        new CreateInvoiceLineRequest(
                                PRODUCT_ID,
                                INVALID_PRODUCT_QUANTITY
                        )
                );

        return Stream.of(
                Arguments.of(
                        BLANK_CUSTOMER_ID,
                        validInvoiceLineRequestList,
                        List.of(CUSTOMER_ID_REQUIRED)
                ),
                Arguments.of(
                        CUSTOMER_ID,
                        emptyInvoiceLineRequestList,
                        List.of(INVOICE_LINES_REQUIRED)
                ),
                Arguments.of(
                        CUSTOMER_ID,
                        invoiceLineRequestListWithBlankProductId,
                        List.of(PRODUCT_ID_REQUIRED.formatted(0))
                ),
                Arguments.of(
                        CUSTOMER_ID,
                        invoiceLineRequestListWithNonPositiveProductQuantity,
                        List.of(PRODUCT_QUANTITY_MUST_BE_POSITIVE.formatted(0))
                ),
                Arguments.of(
                        BLANK_CUSTOMER_ID,
                        emptyInvoiceLineRequestList,
                        List.of(CUSTOMER_ID_REQUIRED, INVOICE_LINES_REQUIRED)
                ),
                Arguments.of(
                        BLANK_CUSTOMER_ID,
                        invoiceLineRequestListWithBlankProductId,
                        List.of(CUSTOMER_ID_REQUIRED, PRODUCT_ID_REQUIRED.formatted(0))
                ),
                Arguments.of(
                        BLANK_CUSTOMER_ID,
                        invoiceLineRequestListWithNonPositiveProductQuantity,
                        List.of(CUSTOMER_ID_REQUIRED, PRODUCT_QUANTITY_MUST_BE_POSITIVE.formatted(0))
                )
        );
    }

    // ---

    private CreateInvoiceRequest createRequest(int lineCount) {
        return new CreateInvoiceRequest(
                CUSTOMER_ID,
                createLineRequestList(lineCount)
        );
    }

    private UpdateInvoiceRequest updateRequest() {
        return new UpdateInvoiceRequest(InvoiceStatus.VALIDATED);
    }

    private InvoiceResponse response(int num) {
        int lineCount = Math.max(1, num);
        return new InvoiceResponse(
                ID + num,
                CUSTOMER_ID + num,
                CUSTOMER_NAME + num,
                ISSUE_TIME,
                InvoiceStatus.CREATED,
                lineResponseList(lineCount),
                totalAmount(lineCount)
        );
    }

    private InvoiceResponse response() {
        return response(1);
    }

    private CreateInvoiceLineRequest createLineRequest(int num) {
        return new CreateInvoiceLineRequest(
                PRODUCT_ID + num,
                PRODUCT_QUANTITY + num
        );
    }

    private List<CreateInvoiceLineRequest> createLineRequestList(int lineCount) {
        return IntStream.range(0, lineCount)
                .mapToObj(this::createLineRequest)
                .toList();
    }

    private InvoiceLineResponse lineResponse(int num) {
        return new InvoiceLineResponse(
                PRODUCT_ID + num,
                PRODUCT_NAME + num,
                PRODUCT_QUANTITY + num,
                PRODUCT_UNIT_PRICE.add(BigDecimal.valueOf(num))
        );
    }

    private InvoiceLineResponse lineResponse() {
        return new InvoiceLineResponse(
                PRODUCT_ID,
                PRODUCT_NAME,
                PRODUCT_QUANTITY,
                PRODUCT_UNIT_PRICE
        );
    }

    private List<InvoiceLineResponse> lineResponseList(int lineCount) {
        return IntStream.range(0, lineCount)
                .mapToObj(this::lineResponse)
                .toList();
    }

    private BigDecimal totalAmount(int lineCount) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < lineCount; ++i) {
            InvoiceLineResponse lineResponse = lineResponse(i);
            totalAmount = totalAmount.add(
                    lineResponse.unitPrice().multiply(
                            BigDecimal.valueOf(lineResponse.quantity()))
            );
        }
        return totalAmount;
    }
}
