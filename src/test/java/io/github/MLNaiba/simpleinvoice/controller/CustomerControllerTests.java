package io.github.MLNaiba.simpleinvoice.controller;

import io.github.MLNaiba.simpleinvoice.dto.CreateCustomerRequest;
import io.github.MLNaiba.simpleinvoice.dto.CustomerResponse;
import io.github.MLNaiba.simpleinvoice.dto.UpdateCustomerRequest;
import io.github.MLNaiba.simpleinvoice.exception.ResourceNotFoundException;
import io.github.MLNaiba.simpleinvoice.service.CustomerService;
import io.github.MLNaiba.simpleinvoice.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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

import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CustomerControllerTests {

    private static final String ID = "id";
    private static final String NAME = "name";

    private static final String UPDATED_NAME = "updated-name";

    private static final String INVALID_ID = "invalid-id";
    private static final String INVALID_NAME = "";

    private static final String BASE_URL = "/api/customers";
    private static final String ID_URL = BASE_URL + "/%s";

    private static final String VALIDATION_FAILED = "Validation failed";
    private static final String NAME_REQUIRED = "name: Customer name must not be blank";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    // Security filter dependencies required by @WebMvcTest
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    public void createCustomer_givenValidRequest_shouldReturnCreatedCustomer() throws Exception {

        // ARRANGE

        CreateCustomerRequest createCustomerRequest = createRequest();
        CustomerResponse customerResponse = response();

        given(customerService.createCustomer(any(CreateCustomerRequest.class)))
                .willReturn(customerResponse);

        // ACT

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCustomerRequest)));

        // ASSERT

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(customerResponse.id()))
                .andExpect(jsonPath("$.name").value(customerResponse.name()));

        verify(customerService).createCustomer(createCustomerRequest);
    }

    @Test
    public void createCustomer_givenInvalidRequestBadParameters_shouldReturnBadRequest() throws Exception {

        // ARRANGE

        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(INVALID_NAME);

        // ACT

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCustomerRequest)));

        // ASSERT

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(VALIDATION_FAILED))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItem(NAME_REQUIRED)));

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    public void getCustomerById_givenValidRequest_shouldReturnCustomer() throws Exception {

        // ARRANGE

        CustomerResponse customerResponse = response();

        given(customerService.getCustomerById(customerResponse.id()))
                .willReturn(customerResponse);

        // ACT

        ResultActions response = mockMvc.perform(
                get(ID_URL.formatted(customerResponse.id())));

        // ASSERT

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerResponse.id()))
                .andExpect(jsonPath("$.name").value(customerResponse.name()));

        verify(customerService).getCustomerById(customerResponse.id());
    }

    @Test
    public void getCustomerById_givenInvalidRequestNonexistentId_shouldReturnNotFound() throws Exception {

        // ARRANGE

        given(customerService.getCustomerById(INVALID_ID))
                .willThrow(new ResourceNotFoundException("Customer", INVALID_ID));

        // ACT

        ResultActions response = mockMvc.perform(
                get(ID_URL.formatted(INVALID_ID)));

        // ASSERT

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString(INVALID_ID)))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(customerService).getCustomerById(INVALID_ID);
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 0})
    public void getAllCustomers_givenValidRequest_shouldReturnCustomerList(
            int responseCount
    ) throws Exception {

        // ARRANGE

        List<CustomerResponse> customerResponses =
                IntStream.range(0, responseCount)
                        .mapToObj(this::response)
                        .toList();

        given(customerService.getAllCustomers())
                .willReturn(customerResponses);

        // ACT

        ResultActions response = mockMvc.perform(get(BASE_URL));

        // ASSERT

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(responseCount));

        for (int i = 0; i < responseCount; ++i) {
            response
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(customerResponses.get(i).id()))
                    .andExpect(jsonPath("$[%d].name".formatted(i)).value(customerResponses.get(i).name()));
        }

        verify(customerService).getAllCustomers();
    }

    @Test
    public void updateCustomer_givenValidRequest_shouldReturnUpdatedCustomer() throws Exception {

        // ARRANGE

        UpdateCustomerRequest updateCustomerRequest = updateRequest();
        CustomerResponse customerResponse = new CustomerResponse(ID, UPDATED_NAME);

        given(customerService.updateCustomer(ID, updateCustomerRequest))
                .willReturn(customerResponse);

        // ACT

        ResultActions response = mockMvc.perform(put(ID_URL.formatted(ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCustomerRequest)));

        // ASSERT

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerResponse.id()))
                .andExpect(jsonPath("$.name").value(customerResponse.name()));

        verify(customerService).updateCustomer(ID, updateCustomerRequest);
    }

    @Test
    public void updateCustomer_givenInvalidRequestBadParameters_shouldReturnBadRequest() throws Exception {

        // ARRANGE

        UpdateCustomerRequest updateCustomerRequest = new UpdateCustomerRequest(INVALID_NAME);

        // ACT

        ResultActions response = mockMvc.perform(put(ID_URL.formatted(ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCustomerRequest)));

        // ASSERT

        response
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(VALIDATION_FAILED))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors", hasItem(NAME_REQUIRED)));

        verify(customerService, never()).updateCustomer(any(String.class), any(UpdateCustomerRequest.class));
    }

    @Test
    public void updateCustomer_givenInvalidRequestNonexistentId_shouldReturnNotFound() throws Exception {

        // ARRANGE

        UpdateCustomerRequest updateCustomerRequest = updateRequest();

        given(customerService.updateCustomer(
                INVALID_ID,
                updateCustomerRequest))
                .willThrow(new ResourceNotFoundException("Customer", INVALID_ID));

        // ACT

        ResultActions response = mockMvc.perform(put(ID_URL.formatted(INVALID_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCustomerRequest)));

        // ASSERT

        response
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString(INVALID_ID)))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(customerService).updateCustomer(
                INVALID_ID,
                updateCustomerRequest);
    }

    @Test
    public void deleteCustomer_givenValidRequest_shouldReturnNoContent() throws Exception {

        // ARRANGE / ACT

        ResultActions response = mockMvc.perform(delete(ID_URL.formatted(ID)));

        // ASSERT

        response.andExpect(status().isNoContent());

        verify(customerService).deleteCustomer(ID);
    }

    @Test
    public void deleteCustomer_givenInvalidRequestNonexistentId_shouldReturnNotFound() throws Exception {

        // ARRANGE

        doThrow(new ResourceNotFoundException("Customer", INVALID_ID))
                .when(customerService)
                .deleteCustomer(INVALID_ID);

        // ACT

        ResultActions response = mockMvc.perform(delete(ID_URL.formatted(INVALID_ID)));

        // ASSERT

        response
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString(INVALID_ID)))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(customerService).deleteCustomer(INVALID_ID);
    }

    // ---

    private CreateCustomerRequest createRequest() {
        return new CreateCustomerRequest(NAME);
    }

    private UpdateCustomerRequest updateRequest() {
        return new UpdateCustomerRequest(UPDATED_NAME);
    }

    private CustomerResponse response() {
        return new CustomerResponse(
                ID,
                NAME
        );
    }

    private CustomerResponse response(int num) {
        return new CustomerResponse(
                ID + num,
                NAME + num
        );
    }
}
