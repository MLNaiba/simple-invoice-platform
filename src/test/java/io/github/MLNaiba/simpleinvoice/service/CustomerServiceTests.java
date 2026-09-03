package io.github.MLNaiba.simpleinvoice.service;

import io.github.MLNaiba.simpleinvoice.domain.Customer;
import io.github.MLNaiba.simpleinvoice.dto.CreateCustomerRequest;
import io.github.MLNaiba.simpleinvoice.dto.CustomerResponse;
import io.github.MLNaiba.simpleinvoice.dto.UpdateCustomerRequest;
import io.github.MLNaiba.simpleinvoice.exception.ResourceNotFoundException;
import io.github.MLNaiba.simpleinvoice.mapper.CustomerMapper;
import io.github.MLNaiba.simpleinvoice.repository.CustomerRepository;
import io.github.MLNaiba.simpleinvoice.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTests {

    private static final String ID = "id";
    private static final String NAME = "name";

    private static final String INVALID_ID = "invalid-id";
    private static final String UPDATED_NAME = "updated-name";

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    @Test
    public void createCustomer_givenValidRequest_shouldReturnCreatedCustomer() {

        // ARRANGE

        Customer customer = customer();

        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // ACT

        CustomerResponse response = customerService.createCustomer(
                new CreateCustomerRequest(customer.getName()));

        // ASSERT

        verify(customerRepository).save(customerCaptor.capture());
        Customer capturedCustomer = customerCaptor.getValue();

        assertThat(capturedCustomer)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(customer);

        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(customer);

        verify(customerMapper).toResponse(customer);
    }

    @Test
    public void getCustomerById_givenValidId_shouldReturnCustomer() {

        // ARRANGE

        Customer customer = customer();

        when(customerRepository.findById(customer.getId()))
                .thenReturn(Optional.of(customer));

        // ACT

        CustomerResponse response = customerService.getCustomerById(customer.getId());

        // ASSERT

        verify(customerRepository).findById(customer.getId());

        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(customer);

        verify(customerMapper).toResponse(customer);
    }

    @Test
    public void getCustomerById_givenInvalidId_shouldThrowResourceNotFoundException() {

        // ARRANGE

        when(customerRepository.findById(INVALID_ID))
                .thenReturn(Optional.empty());

        // ACT / ASSERT

        assertThatThrownBy(() ->
                customerService.getCustomerById(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(INVALID_ID);

        verify(customerRepository).findById(INVALID_ID);

        verify(customerMapper, never()).toResponse(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 0})
    public void getAllCustomers_givenMultipleCustomers_shouldReturnCustomerList(
            int customerCount
    ) {

        // ARRANGE

        List<Customer> customers =
                IntStream.range(0, customerCount)
                        .mapToObj(this::customer)
                        .toList();

        when(customerRepository.findAll()).thenReturn(customers);

        // ACT

        List<CustomerResponse> responses = customerService.getAllCustomers();

        // ASSERT

        verify(customerRepository).findAll();

        assertThat(responses).hasSize(customerCount);

        for (int i = 0; i < customerCount; ++i) {
            assertThat(responses.get(i))
                    .usingRecursiveComparison()
                    .isEqualTo(customers.get(i));
        }

        for (Customer customer : customers) {
            verify(customerMapper).toResponse(customer);
        }
    }

    @Test
    public void updateCustomer_givenValidRequest_shouldReturnUpdatedCustomer() {

        // ARRANGE

        Customer existingCustomer = customer();

        Customer updatedCustomer = Customer.builder()
                .id(existingCustomer.getId())
                .name(UPDATED_NAME)
                .build();

        when(customerRepository.findById(existingCustomer.getId()))
                .thenReturn(Optional.of(existingCustomer));

        when(customerRepository.save(any(Customer.class)))
                .thenReturn(updatedCustomer);

        // ACT

        CustomerResponse response = customerService.updateCustomer(
                existingCustomer.getId(),
                new UpdateCustomerRequest(UPDATED_NAME));

        // ASSERT

        verify(customerRepository).findById(existingCustomer.getId());

        verify(customerRepository).save(customerCaptor.capture());
        Customer capturedCustomer = customerCaptor.getValue();
        assertThat(capturedCustomer)
                .usingRecursiveComparison()
                .isEqualTo(updatedCustomer);

        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(updatedCustomer);

        verify(customerMapper).toResponse(updatedCustomer);
    }

    @Test
    public void updateCustomer_givenInvalidId_shouldThrowResourceNotFoundException() {

        // ARRANGE

        when(customerRepository.findById(INVALID_ID))
                .thenReturn(Optional.empty());

        // ACT / ASSERT

        assertThatThrownBy(() ->
                customerService.updateCustomer(INVALID_ID, new UpdateCustomerRequest(UPDATED_NAME)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(INVALID_ID);

        verify(customerRepository).findById(INVALID_ID);
        verify(customerRepository, never()).save(any(Customer.class));

        verify(customerMapper, never()).toResponse(any());
    }

    @Test
    public void deleteCustomer_givenValidId_shouldDeleteCustomer() {

        // ARRANGE

        Customer customer = customer();

        when(customerRepository.findById(customer.getId()))
                .thenReturn(Optional.of(customer));

        // ACT

        customerService.deleteCustomer(customer.getId());

        // ASSERT

        verify(customerRepository).findById(customer.getId());
        verify(customerRepository).delete(customer);

        verify(customerMapper, never()).toResponse(any());
    }

    @Test
    public void deleteCustomer_givenInvalidId_shouldThrowResourceNotFoundException() {

        // ARRANGE

        when(customerRepository.findById(INVALID_ID))
                .thenReturn(Optional.empty());

        // ACT / ASSERT

        assertThatThrownBy(() ->
                customerService.deleteCustomer(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(INVALID_ID);

        verify(customerRepository).findById(INVALID_ID);
        verify(customerRepository, never()).delete(any(Customer.class));

        verify(customerMapper, never()).toResponse(any());
    }

    // ---

    @BeforeEach
    public void setUp() {
        lenient()
                .when(customerMapper.toResponse(any(Customer.class)))
                .thenAnswer(invocation -> {
                    Customer customer = invocation.getArgument(0);
                    return new CustomerResponse(
                            customer.getId(),
                            customer.getName()
                    );
                });
    }

    // ---

    private Customer customer(int num) {
        return Customer.builder()
                .id(ID + num)
                .name(NAME + num)
                .build();
    }

    private Customer customer() {
        return Customer.builder()
                .id(ID)
                .name(NAME)
                .build();
    }
}
