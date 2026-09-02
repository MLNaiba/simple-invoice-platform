package io.github.MLNaiba.simpleinvoice.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Getter @Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("Product")
public class Product {
    @Id
    private String id;

    private String name;

    private BigDecimal price;
}
