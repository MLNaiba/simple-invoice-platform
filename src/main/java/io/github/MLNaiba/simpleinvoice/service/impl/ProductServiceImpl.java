package io.github.MLNaiba.simpleinvoice.service.impl;

import io.github.MLNaiba.simpleinvoice.domain.Product;
import io.github.MLNaiba.simpleinvoice.dto.CreateProductRequest;
import io.github.MLNaiba.simpleinvoice.dto.ProductResponse;
import io.github.MLNaiba.simpleinvoice.dto.UpdateProductRequest;
import io.github.MLNaiba.simpleinvoice.exception.ResourceNotFoundException;
import io.github.MLNaiba.simpleinvoice.mapper.ProductMapper;
import io.github.MLNaiba.simpleinvoice.repository.ProductRepository;
import io.github.MLNaiba.simpleinvoice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info(
                "Creating product with name [{}]",
                request.name()
        );

        Product product = Product.builder()
                .name(request.name())
                .price(request.price())
                .build();

        Product savedProduct = productRepository.save(product);

        log.info(
                "Product with name [{}] successfully created",
                savedProduct.getName()
        );

        return productMapper.toResponse(savedProduct);
    }

    @Override
    public ProductResponse getProductById(String id) {
        log.info(
                "Retrieving product with id [{}]",
                id
        );

        Product product = findById(id);

        log.info(
                "Product with id [{}] successfully retrieved",
                id
        );

        return productMapper.toResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        log.info(
                "Retrieving all products"
        );

        List<Product> products = productRepository.findAll();

        log.info(
                "All products successfully retrieved"
        );

        return products.stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public ProductResponse updateProduct(String id, UpdateProductRequest request) {
        log.info(
                "Updating product with id [{}]",
                id
        );

        Product product = findById(id);

        product.setName(request.name());
        product.setPrice(request.price());

        Product savedProduct = productRepository.save(product);

        log.info(
                "Product with id [{}] successfully updated",
                id
        );

        return productMapper.toResponse(savedProduct);
    }

    @Override
    public void deleteProduct(String id) {
        log.info(
                "Deleting product with id [{}]",
                id
        );

        Product product = findById(id);

        productRepository.delete(product);

        log.info(
                "Product with id [{}] successfully deleted",
                id
        );
    }

    // ---

    private Product findById(String id)
            throws ResourceNotFoundException {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
}
