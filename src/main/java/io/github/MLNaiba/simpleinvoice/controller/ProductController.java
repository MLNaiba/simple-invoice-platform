package io.github.MLNaiba.simpleinvoice.controller;

import io.github.MLNaiba.simpleinvoice.dto.CreateProductRequest;
import io.github.MLNaiba.simpleinvoice.dto.ProductResponse;
import io.github.MLNaiba.simpleinvoice.dto.UpdateProductRequest;
import io.github.MLNaiba.simpleinvoice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        return productService.createProduct(request);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(
            @PathVariable String id) {
        return productService.getProductById(id);
    }

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(
            @PathVariable String id) {
        productService.deleteProduct(id);
    }
}