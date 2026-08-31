package io.github.MLNaiba.simpleinvoice.service;

import io.github.MLNaiba.simpleinvoice.dto.CreateCustomerRequest;
import io.github.MLNaiba.simpleinvoice.dto.CustomerResponse;
import io.github.MLNaiba.simpleinvoice.dto.UpdateCustomerRequest;

import java.util.List;

public interface CustomerService {

    CustomerResponse createCustomer(CreateCustomerRequest request);

    CustomerResponse getCustomerById(String id);

    List<CustomerResponse> getAllCustomers();

    CustomerResponse updateCustomer(String id, UpdateCustomerRequest request);

    void deleteCustomer(String id);
}
