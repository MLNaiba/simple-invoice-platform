package io.github.MLNaiba.simpleinvoice.mapper;

import io.github.MLNaiba.simpleinvoice.domain.Product;
import io.github.MLNaiba.simpleinvoice.dto.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice()
        );
    }
}
