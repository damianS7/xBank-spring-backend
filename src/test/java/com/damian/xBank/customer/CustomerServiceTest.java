package com.damian.xBank.customer;

import com.damian.xBank.customer.exception.CustomerException;
import com.damian.xBank.customer.http.request.CustomerRegistrationRequest;
import com.damian.xBank.customer.http.request.CustomerUpdateRequest;
import com.damian.xBank.profile.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

// Habilita Mockito en JUnit 5
@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(
                customerRepository,
                bCryptPasswordEncoder
        );
        customerRepository.deleteAll();

        Customer customer = new Customer("david@gmail.com", "123456");

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                customer, null, Collections.emptyList()));
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
        assertThat(result.get(0).email()).isEqualTo("alicia@gmail.com");
        assertThat(result.get(1).email()).isEqualTo("david@gmail.com");

        // Verificar que el método findAll() fue llamado una vez
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void shouldNotGetAnyCustomers() {
        // given
        List<Customer> customers = List.of();

        // when
        when(customerRepository.findAll()).thenReturn(customers);

        // Llamar al método
        List<CustomerDTO> result = customerService.getCustomers();

        // then
        assertThat(result.size()).isEqualTo(0);

        // Verificar que el método findAll() fue llamado una vez
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void shouldGetOneCustomer() {
        // Given
        Customer customer = new Customer(
                1L,
                "alice@gmail.com",
                "1234"
        );

        // When
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        Customer storedCustomer = customerService.getCustomer(customer.getId());

        // Then
        assertEquals(customer.getId(), storedCustomer.getId());
        assertEquals("alice@gmail.com", storedCustomer.getEmail());

        verify(customerRepository, times(1)).findById(customer.getId());
    }

    @Test
    void shouldNotGetAnyCustomerWhenNotExistAndWillThrow() {
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
    void shouldCreateCustomer() {
        // given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "david@gmail.com",
                "123456",
                "david",
                "white",
                "123 123 123",
                "1/1/1980",
                Gender.MALE,
                "",
                "Fake AV",
                "50120",
                "USA",
                "123123123Z"
        );

        final String passwordHash = "¢5554ml;f;lsd";

        // when
        when(bCryptPasswordEncoder.encode(request.password())).thenReturn(passwordHash);
        when(customerRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        customerService.createCustomer(request);

        // then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerArgumentCaptor.capture());

        Customer customer = customerArgumentCaptor.getValue();
        verify(customerRepository, times(1)).save(customer);
        assertThat(customer.getId()).isNull();
        assertThat(customer.getEmail()).isEqualTo(request.email());
        assertThat(customer.getPassword()).isEqualTo(passwordHash);
    }

    @Test
    void shouldNotCreateAnyCustomerWhenEmailIsTakenAndWillThrow() {
        // given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "david@gmail.com",
                "123456",
                "david",
                "white",
                "123 123 123",
                "1/1/1980",
                Gender.MALE,
                "",
                "Fake AV",
                "50120",
                "USA",
                "123123123Z"
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
    void shouldDeleteCustomer() {
        // given
        Long id = 7L;
        when(customerRepository.existsById(id)).thenReturn(true);

        // when
        boolean isDeleted = customerService.deleteCustomer(id);

        // then
        verify(customerRepository, times(1)).deleteById(id);
        verify(customerRepository).deleteById(id);
        assertThat(isDeleted).isTrue();
    }

    @Test
    void shouldNotDeleteCustomerWhenNotExistAndThrow() {
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
    void shouldUpdateCustomerPasswordOnly() {
        // Given
        String currentRawPassword = "123456";
        String currentEncodedPassword = "encodedActualPassword";
        String rawNewPassword = "1234";
        String encodedNewPassword = "encodedNewPassword";

        Customer customer = new Customer(
                10L,
                "david@gmail.com",
                currentEncodedPassword
        );
        Customer storedCustomer = new Customer();

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                "david@gmail.com",
                null,
                currentRawPassword,
                rawNewPassword
        );

        when(bCryptPasswordEncoder.encode(rawNewPassword)).thenReturn(encodedNewPassword);
        when(bCryptPasswordEncoder.matches(currentRawPassword, currentEncodedPassword)).thenReturn(true);

        when(customerRepository.findByEmail(updateRequest.currentEmail())).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(storedCustomer);

        customerService.updateCustomer(updateRequest);

        // Then
        verify(customerRepository, times(1)).save(customer);
        assertThat(customer.getEmail()).isEqualTo(updateRequest.currentEmail());
        assertThat(customer.getPassword()).isEqualTo(encodedNewPassword);
    }

    @Test
    void shouldNotUpdatePasswordWhenCurrentPasswordDoesNotMatch() {
        // Given
        Customer customer = new Customer(
                10L,
                "david@gmail.com",
                "123456"
        );

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                "david@gmail.com",
                null,
                "wrongPassword",
                "1234"
        );

        // when
        when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(bCryptPasswordEncoder.matches(updateRequest.currentPassword(), customer.getPassword())).thenReturn(false);
        CustomerException ex = assertThrows(CustomerException.class,
                () -> customerService.updateCustomer(
                        updateRequest
                )
        );
        // Then
        assertThat(ex.getMessage()).isEqualTo("Password does not match.");
    }

    @Test
    void shouldUpdateCustomerEmailOnly() {
        // Given
        String currentRawPassword = "123456";
        String currentEncodedPassword = "encodedActualPassword";

        Customer customer = new Customer(
                10L,
                "david@gmail.com",
                currentEncodedPassword
        );
        Customer storedCustomer = new Customer();

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                "david@gmail.com",
                "david@outlook.com",
                currentRawPassword,
                null
        );

        // when
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                customer, null, Collections.emptyList()));
        when(bCryptPasswordEncoder.matches(currentRawPassword, currentEncodedPassword)).thenReturn(true);
        when(customerRepository.findByEmail(updateRequest.currentEmail())).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(storedCustomer);

        customerService.updateCustomer(updateRequest);

        // Then
        verify(customerRepository, times(1)).save(customer);
        assertThat(customer.getEmail()).isEqualTo(updateRequest.newEmail());
    }
}
