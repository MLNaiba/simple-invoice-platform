package io.github.MLNaiba.simpleinvoice.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("User")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @ToString.Exclude
    private String password;

    private UserRole role;
}
