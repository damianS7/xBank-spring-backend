package com.damian.xBank.customer;

import com.damian.xBank.customer.exception.CustomerException;
import com.damian.xBank.customer.http.request.CustomerRegistrationRequest;
import com.damian.xBank.customer.http.request.CustomerUpdateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito en JUnit 5
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, bCryptPasswordEncoder);
        customerRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void shouldGetAllCustomers() {
        // given
        List<Customer> customers = List.of(
                new Customer(1L, "alicia@gmail.com", "123455"),
                new Customer(2L, "david@gmail.com", "123456")
        );

        // when
        when(customerRepository.findAll()).thenReturn(customers);

        // Llamar al método
        List<CustomerDTO> result = customerService.getCustomers();

        // then
        assertNotNull(result);
        assertThat(customers.size()).isEqualTo(2);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).email()).isEqualTo("alicia@gmail.com");
        assertThat(result.get(1).email()).isEqualTo("david@gmail.com");

        // Verificar que el método findAll() fue llamado una vez
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void shouldGetZeroCustomers() {
        // given
        List<Customer> customers = List.of();

        // when
        when(customerRepository.findAll()).thenReturn(customers);

        // Llamar al método
        List<CustomerDTO> result = customerService.getCustomers();

        // then
        assertNotNull(result);
        assertThat(result.size()).isEqualTo(0);

        // Verificar que el método findAll() fue llamado una vez
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void canGetCustomer() {
        // Given
        Customer customer = new Customer(1L, "alice@gmail.com", "1234");
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        // When
        Customer storedCustomer = customerService.getCustomer(customer.getId());

        // Then
        assertNotNull(storedCustomer);
        assertEquals(customer.getId(), storedCustomer.getId());
        assertEquals("alice@gmail.com", storedCustomer.getEmail());

        verify(customerRepository, times(1)).findById(customer.getId());
    }

    @Test
    void cannotGetCustomerWhenNotExistAndWillThrow() {
        // Given
        Long id = -1L;

        // When
        // Then
        CustomerException exception = assertThrows(CustomerException.class,
                () -> customerService.getCustomer(id)
        );

        assertEquals("Customer not found.", exception.getMessage());
    }

    @Test
    void canCreateCustomer() {
        // given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "david@gmail.com",
                "123456"
        );

        // when
        when(customerRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        customerService.createCustomer(request);
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);

        // then
        verify(customerRepository).save(customerArgumentCaptor.capture());

        Customer customer = customerArgumentCaptor.getValue();
        verify(customerRepository, times(1)).save(customer);

        assertThat(customer.getId()).isNull();
        assertThat(customer.getEmail()).isEqualTo(request.email());
    }

    @Test
    void cannotCreateCustomerWhenEmailIsTakenAndWillThrow() {
        // given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "david@gmail.com",
                "123456"
        );

        // when
        when(customerRepository.findByEmail(request.email())).thenReturn(Optional.of(new Customer()));
        CustomerException exception = assertThrows(CustomerException.class,
                () -> customerService.createCustomer(request)
        );

        // then
        verify(customerRepository, times(0)).save(any());
        assertEquals("Email is taken.", exception.getMessage());
    }

    @Test
    void canDeleteCustomer() {
        // given
        Long id = 7L;
        when(customerRepository.existsById(id)).thenReturn(true);

        // when
        boolean isDeleted = customerService.deleteCustomer(id);

        // then
        verify(customerRepository, times(1)).deleteById(id);
        assertThat(isDeleted).isTrue();
    }

    @Test
    void cannotDeleteCustomerWhenNotExistAndThrow() {
        // given
        Long id = 7L;

        // when
        when(customerRepository.existsById(id)).thenReturn(false);

        // then
        CustomerException ex = assertThrows(CustomerException.class,
                () -> customerService.deleteCustomer(id)
        );
        assertThat(ex.getMessage()).isEqualTo("Customer do not exist.");
        verify(customerRepository, never()).deleteById(anyLong());
    }

    @Test
    void canUpdateCustomer() {
        // Given
        String rawActualPassword = "123456";
        String encodedActualPassword = "encodedActualPassword";
        String rawNewPassword = "1234";
        String encodedNewPassword = "encodedNewPassword";

        Customer customer = new Customer(10L, "david@gmail.com", encodedActualPassword);
        Customer storedCustomer = new Customer();

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                "david@outlook.com",
                rawActualPassword,
                rawNewPassword
        );

        when(bCryptPasswordEncoder.encode(rawNewPassword)).thenReturn(encodedNewPassword);
        when(bCryptPasswordEncoder.matches(rawActualPassword, encodedActualPassword)).thenReturn(true);

        when(customerRepository.findByEmail(updateRequest.email())).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(storedCustomer);

        customerService.updateCustomer(
                updateRequest.email(),
                updateRequest.actualPassword(),
                updateRequest.newPassword()
        );

        // Then
        verify(customerRepository, times(1)).save(customer);
        assertThat(customer.getEmail()).isEqualTo(updateRequest.email());
    }

    @Test
    void cannotUpdateCustomerWhenCurrentPasswordDoesNotMatch() {
        // Given
        Customer customer = new Customer(
                10L,
                "david@gmail.com",
                "123456"
        );

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                "david@gmail.com",
                "wrongPassword",
                "1234"
        );

        // when
        when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(bCryptPasswordEncoder.matches(updateRequest.actualPassword(), customer.getPassword())).thenReturn(false);
        CustomerException ex = assertThrows(CustomerException.class,
                () -> customerService.updateCustomer(
                        updateRequest.email(),
                        updateRequest.actualPassword(),
                        updateRequest.newPassword()
                )
        );
        // Then
        assertThat(ex.getMessage()).isEqualTo("Password does not match.");
//        verify(customerRepository, never()).save(anyLong(), anyString());
    }
}
