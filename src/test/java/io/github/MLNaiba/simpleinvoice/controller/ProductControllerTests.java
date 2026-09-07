package io.github.MLNaiba.simpleinvoice.controller;

import io.github.MLNaiba.simpleinvoice.dto.CreateProductRequest;
import io.github.MLNaiba.simpleinvoice.dto.ProductResponse;
import io.github.MLNaiba.simpleinvoice.dto.UpdateProductRequest;
import io.github.MLNaiba.simpleinvoice.exception.ResourceNotFoundException;
import io.github.MLNaiba.simpleinvoice.service.JwtService;
import io.github.MLNaiba.simpleinvoice.service.ProductService;
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

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTests {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final BigDecimal PRICE = BigDecimal.valueOf(100);

    private static final String UPDATED_NAME = "updated-name";
    private static final BigDecimal UPDATED_PRICE = BigDecimal.valueOf(200);

    private static final String INVALID_ID = "invalid-id";
    private static final String INVALID_NAME = "";
    private static final BigDecimal NEGATIVE_PRICE = BigDecimal.valueOf(-1);

    private static final String BASE_URL = "/api/products";
    private static final String ID_URL = BASE_URL + "/%s";

    private static final String VALIDATION_FAILED = "Validation failed";
    private static final String NAME_REQUIRED = "name: Product name must not be blank";
    private static final String PRICE_REQUIRED = "price: Product price must not be null";
    private static final String PRICE_MUST_BE_POSITIVE = "price: Product price must be positive";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    // Security filter dependencies required by @WebMvcTest
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    public void createProduct_givenValidRequest_shouldReturnCreatedProduct() throws Exception {

        // ARRANGE

        CreateProductRequest createProductRequest = createRequest();
        ProductResponse productResponse = response();

        given(productService.createProduct(any(CreateProductRequest.class)))
                .willReturn(productResponse);

        // ACT

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createProductRequest)));

        // ASSERT

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(productResponse.id()))
                .andExpect(jsonPath("$.name").value(productResponse.name()))
                .andExpect(jsonPath("$.price").value(productResponse.price()));

        verify(productService).createProduct(createProductRequest);
    }

    @ParameterizedTest
    @MethodSource("invalidProductData")
    public void createProduct_givenInvalidRequestBadParameters_shouldReturnBadRequest(
            String name,
            BigDecimal price,
            List<String> expectedErrors
    ) throws Exception {

        // ARRANGE

        CreateProductRequest createProductRequest = new CreateProductRequest(
                name,
                price
        );

        // ACT

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createProductRequest)));

        // ASSERT

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(VALIDATION_FAILED))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(expectedErrors.size())));

        for (String expectedError : expectedErrors) {
            response.andExpect(jsonPath("$.errors", hasItem(expectedError)));
        }

        verify(productService, never()).createProduct(any());
    }

    @Test
    public void getProductById_givenValidRequest_shouldReturnProduct() throws Exception {

        // ARRANGE

        ProductResponse productResponse = response();

        given(productService.getProductById(productResponse.id()))
                .willReturn(productResponse);

        // ACT

        ResultActions response = mockMvc.perform(
                get(ID_URL.formatted(productResponse.id())));

        // ASSERT

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productResponse.id()))
                .andExpect(jsonPath("$.name").value(productResponse.name()))
                .andExpect(jsonPath("$.price").value(productResponse.price()));

        verify(productService).getProductById(productResponse.id());
    }

    @Test
    public void getProductById_givenInvalidRequestNonexistentId_shouldReturnNotFound() throws Exception {

        // ARRANGE

        given(productService.getProductById(INVALID_ID))
                .willThrow(new ResourceNotFoundException("Product", INVALID_ID));

        // ACT

        ResultActions response = mockMvc.perform(
                get(ID_URL.formatted(INVALID_ID)));

        // ASSERT

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString(INVALID_ID)))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(productService).getProductById(INVALID_ID);
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 0})
    public void getAllProducts_givenValidRequest_shouldReturnProductList(
            int responseCount
    ) throws Exception {

        // ARRANGE

        List<ProductResponse> productResponses =
                IntStream.range(0, responseCount)
                        .mapToObj(this::response)
                        .toList();

        given(productService.getAllProducts())
                .willReturn(productResponses);

        // ACT

        ResultActions response = mockMvc.perform(get(BASE_URL));

        // ASSERT

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(responseCount));

        for (int i = 0; i < responseCount; ++i) {
            response
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(productResponses.get(i).id()))
                    .andExpect(jsonPath("$[%d].name".formatted(i)).value(productResponses.get(i).name()))
                    .andExpect(jsonPath("$[%d].price".formatted(i)).value(productResponses.get(i).price()));
        }

        verify(productService).getAllProducts();
    }

    @Test
    public void updateProduct_givenValidRequest_shouldReturnUpdatedProduct() throws Exception {

        // ARRANGE

        UpdateProductRequest updateProductRequest = updateRequest();
        ProductResponse productResponse = new ProductResponse(ID, UPDATED_NAME, UPDATED_PRICE);

        given(productService.updateProduct(ID, updateProductRequest))
                .willReturn(productResponse);

        // ACT

        ResultActions response = mockMvc.perform(put(ID_URL.formatted(ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProductRequest)));

        // ASSERT

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productResponse.id()))
                .andExpect(jsonPath("$.name").value(productResponse.name()))
                .andExpect(jsonPath("$.price").value(productResponse.price()));

        verify(productService).updateProduct(ID, updateProductRequest);
    }

    @ParameterizedTest
    @MethodSource("invalidProductData")
    public void updateProduct_givenInvalidRequestBadParameters_shouldReturnBadRequest(
            String name,
            BigDecimal price,
            List<String> expectedErrors
    ) throws Exception {

        // ARRANGE

        UpdateProductRequest updateProductRequest = new UpdateProductRequest(
                name,
                price
        );

        // ACT

        ResultActions response = mockMvc.perform(put(ID_URL.formatted(ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProductRequest)));

        // ASSERT

        response
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(VALIDATION_FAILED))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(expectedErrors.size())));

        for (String expectedError : expectedErrors) {
            response.andExpect(jsonPath("$.errors", hasItem(expectedError)));
        }

        verify(productService, never()).updateProduct(any(String.class), any(UpdateProductRequest.class));
    }

    @Test
    public void updateProduct_givenInvalidRequestNonexistentId_shouldReturnNotFound() throws Exception {

        // ARRANGE

        UpdateProductRequest updateProductRequest = updateRequest();

        given(productService.updateProduct(
                INVALID_ID,
                updateProductRequest))
                .willThrow(new ResourceNotFoundException("Product", INVALID_ID));

        // ACT

        ResultActions response = mockMvc.perform(put(ID_URL.formatted(INVALID_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProductRequest)));

        // ASSERT

        response
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString(INVALID_ID)))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(productService).updateProduct(
                INVALID_ID,
                updateProductRequest);
    }

    @Test
    public void deleteProduct_givenValidRequest_shouldReturnNoContent() throws Exception {

        // ARRANGE / ACT

        ResultActions response = mockMvc.perform(delete(ID_URL.formatted(ID)));

        // ASSERT

        response.andExpect(status().isNoContent());

        verify(productService).deleteProduct(ID);
    }

    @Test
    public void deleteProduct_givenInvalidRequestNonexistentId_shouldReturnNotFound() throws Exception {

        // ARRANGE

        doThrow(new ResourceNotFoundException("Product", INVALID_ID))
                .when(productService)
                .deleteProduct(INVALID_ID);

        // ACT

        ResultActions response = mockMvc.perform(delete(ID_URL.formatted(INVALID_ID)));

        // ASSERT

        response
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString(INVALID_ID)))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(productService).deleteProduct(INVALID_ID);
    }

    // ---

    private static Stream<Arguments> invalidProductData() {
        return Stream.of(
                Arguments.of(
                        INVALID_NAME,
                        PRICE,
                        List.of(NAME_REQUIRED)
                ),
                Arguments.of(
                        NAME,
                        null,
                        List.of(PRICE_REQUIRED)
                ),
                Arguments.of(
                        NAME,
                        NEGATIVE_PRICE,
                        List.of(PRICE_MUST_BE_POSITIVE)
                ),
                Arguments.of(
                        INVALID_NAME,
                        null,
                        List.of(NAME_REQUIRED, PRICE_REQUIRED)
                ),
                Arguments.of(
                        INVALID_NAME,
                        NEGATIVE_PRICE,
                        List.of(NAME_REQUIRED, PRICE_MUST_BE_POSITIVE)
                )
        );
    }

    // ---

    private CreateProductRequest createRequest() {
        return new CreateProductRequest(NAME, PRICE);
    }

    private UpdateProductRequest updateRequest() {
        return new UpdateProductRequest(UPDATED_NAME, UPDATED_PRICE);
    }

    private ProductResponse response() {
        return new ProductResponse(
                ID,
                NAME,
                PRICE
        );
    }

    private ProductResponse response(int num) {
        return new ProductResponse(
                ID + num,
                NAME + num,
                PRICE.add(BigDecimal.valueOf(num))
        );
    }
}