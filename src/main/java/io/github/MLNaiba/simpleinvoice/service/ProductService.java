package io.github.MLNaiba.simpleinvoice.service;

import io.github.MLNaiba.simpleinvoice.dto.CreateProductRequest;
import io.github.MLNaiba.simpleinvoice.dto.ProductResponse;
import io.github.MLNaiba.simpleinvoice.dto.UpdateProductRequest;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse getProductById(String id);

    List<ProductResponse> getAllProducts();

    ProductResponse updateProduct(String id, UpdateProductRequest request);

    void deleteProduct(String id);
}
