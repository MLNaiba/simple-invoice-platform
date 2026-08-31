package io.github.MLNaiba.simpleinvoice.repository;

import io.github.MLNaiba.simpleinvoice.domain.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
}
