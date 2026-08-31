package io.github.MLNaiba.simpleinvoice.mapper;

import io.github.MLNaiba.simpleinvoice.domain.Customer;
import io.github.MLNaiba.simpleinvoice.dto.CustomerResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName()
        );
    }
}
