package io.github.MLNaiba.simpleinvoice.service.impl;

import io.github.MLNaiba.simpleinvoice.domain.Customer;
import io.github.MLNaiba.simpleinvoice.dto.CreateCustomerRequest;
import io.github.MLNaiba.simpleinvoice.dto.CustomerResponse;
import io.github.MLNaiba.simpleinvoice.dto.UpdateCustomerRequest;
import io.github.MLNaiba.simpleinvoice.exception.ResourceNotFoundException;
import io.github.MLNaiba.simpleinvoice.mapper.CustomerMapper;
import io.github.MLNaiba.simpleinvoice.repository.CustomerRepository;
import io.github.MLNaiba.simpleinvoice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;

    @Override
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        log.info(
                "Creating customer with name [{}]",
                request.name()
        );

        Customer customer = Customer.builder()
                .name(request.name())
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        log.info(
                "Customer with name [{}] successfully created",
                savedCustomer.getName()
        );

        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    public CustomerResponse getCustomerById(String id) {
        log.info(
                "Retrieving customer with id [{}]",
                id
        );

        Customer customer = findById(id);

        log.info(
                "Customer with id [{}] successfully retrieved",
                id
        );

        return customerMapper.toResponse(customer);
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {
        log.info(
                "Retrieving all customers"
        );

        List<Customer> customers = customerRepository.findAll();

        log.info(
                "All customers successfully retrieved"
        );

        return customers.stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    public CustomerResponse updateCustomer(String id, UpdateCustomerRequest request) {
        log.info(
                "Updating customer with id [{}]",
                id
        );

        Customer customer = findById(id);

        customer.setName(request.name());

        Customer savedCustomer = customerRepository.save(customer);

        log.info(
                "Customer with id [{}] successfully updated",
                id
        );

        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    public void deleteCustomer(String id) {
        log.info(
                "Deleting customer with id [{}]",
                id
        );

        Customer customer = findById(id);

        customerRepository.delete(customer);

        log.info(
                "Customer with id [{}] successfully deleted",
                id
        );
    }

    // ---

    private Customer findById(String id)
            throws ResourceNotFoundException {
        return customerRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }
}
