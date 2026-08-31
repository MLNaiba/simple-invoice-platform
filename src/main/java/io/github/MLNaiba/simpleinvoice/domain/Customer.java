package io.github.MLNaiba.simpleinvoice.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter @Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("Customer")
public class Customer {
    @Id
    private String id;

    private String name;
}
