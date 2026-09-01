package io.github.MLNaiba.simpleinvoice.service;

import io.github.MLNaiba.simpleinvoice.domain.Product;
import io.github.MLNaiba.simpleinvoice.dto.CreateProductRequest;
import io.github.MLNaiba.simpleinvoice.dto.ProductResponse;
import io.github.MLNaiba.simpleinvoice.dto.UpdateProductRequest;
import io.github.MLNaiba.simpleinvoice.exception.ResourceNotFoundException;
import io.github.MLNaiba.simpleinvoice.mapper.ProductMapper;
import io.github.MLNaiba.simpleinvoice.repository.ProductRepository;
import io.github.MLNaiba.simpleinvoice.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final BigDecimal PRICE = BigDecimal.valueOf(100);

    private static final String INVALID_ID = "invalid-id";
    private static final String UPDATED_NAME = "updated-name";
    private static final BigDecimal UPDATED_PRICE = BigDecimal.valueOf(200);

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    @Test
    public void createProduct_givenValidRequest_shouldReturnCreatedProduct() {

        // ARRANGE

        Product product = product();

        when(productRepository.save(any(Product.class))).thenReturn(product);

        // ACT

        ProductResponse response = productService.createProduct(
                new CreateProductRequest(
                        product.getName(),
                        product.getPrice()
                )
        );

        // ASSERT

        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();

        assertThat(capturedProduct)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(product);

        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(product);

        verify(productMapper).toResponse(product);
    }

    @Test
    public void getProductById_givenValidId_shouldReturnProduct() {

        // ARRANGE

        Product product = product();

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        // ACT

        ProductResponse response = productService.getProductById(product.getId());

        // ASSERT

        verify(productRepository).findById(product.getId());

        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(product);

        verify(productMapper).toResponse(product);
    }

    @Test
    public void getProductById_givenInvalidId_shouldThrowResourceNotFoundException() {

        // ARRANGE

        when(productRepository.findById(INVALID_ID))
                .thenReturn(Optional.empty());

        // ACT / ASSERT

        assertThatThrownBy(() ->
                productService.getProductById(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(INVALID_ID);

        verify(productRepository).findById(INVALID_ID);

        verify(productMapper, never()).toResponse(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 0})
    public void getAllProducts_givenMultipleProducts_shouldReturnProductList(
            int productCount
    ) {

        // ARRANGE

        List<Product> products =
                IntStream.range(0, productCount)
                        .mapToObj(this::product)
                        .toList();

        when(productRepository.findAll()).thenReturn(products);

        // ACT

        List<ProductResponse> responses = productService.getAllProducts();

        // ASSERT

        verify(productRepository).findAll();

        assertThat(responses).hasSize(productCount);

        for (int i = 0; i < productCount; ++i) {
            assertThat(responses.get(i))
                    .usingRecursiveComparison()
                    .isEqualTo(products.get(i));
        }

        for (Product product : products) {
            verify(productMapper).toResponse(product);
        }
    }

    @Test
    public void updateProduct_givenValidRequest_shouldReturnUpdatedProduct() {

        // ARRANGE

        Product existingProduct = product();

        Product updatedProduct = Product.builder()
                .id(existingProduct.getId())
                .name(UPDATED_NAME)
                .price(UPDATED_PRICE)
                .build();

        when(productRepository.findById(existingProduct.getId()))
                .thenReturn(Optional.of(existingProduct));

        when(productRepository.save(any(Product.class)))
                .thenReturn(updatedProduct);

        // ACT

        ProductResponse response = productService.updateProduct(
                existingProduct.getId(),
                new UpdateProductRequest(
                        UPDATED_NAME,
                        UPDATED_PRICE
                )
        );

        // ASSERT

        verify(productRepository).findById(existingProduct.getId());

        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();
        assertThat(capturedProduct)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(updatedProduct);

        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(updatedProduct);

        verify(productMapper).toResponse(updatedProduct);

    }

    @Test
    public void updateProduct_givenInvalidId_shouldThrowResourceNotFoundException() {

        // ARRANGE

        when(productRepository.findById(INVALID_ID))
                .thenReturn(Optional.empty());

        // ACT / ASSERT

        assertThatThrownBy(() ->
                productService.updateProduct(INVALID_ID, new UpdateProductRequest(
                        UPDATED_NAME,
                        UPDATED_PRICE
                )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(INVALID_ID);

        verify(productRepository).findById(INVALID_ID);
        verify(productRepository, never()).save(any(Product.class));

        verify(productMapper, never()).toResponse(any());
    }

    @Test
    public void deleteProduct_givenValidId_shouldDeleteProduct() {

        // ARRANGE

        Product product = product();

        when(productRepository.findById(product.getId()))
                .thenReturn(Optional.of(product));

        // ACT

        productService.deleteProduct(product.getId());

        // ASSERT

        verify(productRepository).findById(product.getId());
        verify(productRepository).delete(product);

        verify(productMapper, never()).toResponse(any());
    }

    @Test
    public void deleteProduct_givenInvalidId_shouldThrowResourceNotFoundException() {

        // ARRANGE

        when(productRepository.findById(INVALID_ID))
                .thenReturn(Optional.empty());

        // ACT / ASSERT

        assertThatThrownBy(() ->
                productService.deleteProduct(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(INVALID_ID);

        verify(productRepository).findById(INVALID_ID);
        verify(productRepository, never()).delete(any(Product.class));

        verify(productMapper, never()).toResponse(any());
    }

    // ---

    @BeforeEach
    public void setUp() {
        lenient().when(productMapper.toResponse(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product product = invocation.getArgument(0);
                    return new ProductResponse(
                            product.getId(),
                            product.getName(),
                            product.getPrice());
                });
    }

    // ---

    private Product product(int num) {
        return Product.builder()
                .id(ID + num)
                .name(NAME + num)
                .price(PRICE.add(BigDecimal.valueOf(num)))
                .build();
    }

    private Product product() {
        return Product.builder()
                .id(ID)
                .name(NAME)
                .price(PRICE)
                .build();
    }
}
