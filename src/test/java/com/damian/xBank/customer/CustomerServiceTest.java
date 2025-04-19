package com.damian.xBank.customer;

import com.damian.xBank.customer.exception.CustomerException;
import com.damian.xBank.customer.http.request.CustomerRegistrationRequest;
import com.damian.xBank.customer.http.request.CustomerUpdateRequest;
import com.damian.xBank.customer.profile.CustomerGender;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

// TODO cleanup
@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private String hashedPassword = "3hri2rhid;/!";

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(
                customerRepository,
                bCryptPasswordEncoder
        );
        customerRepository.deleteAll();

        customer = new Customer(99L, "customer@test.com", this.hashedPassword);

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

        List<CustomerDTO> result = customerService.getCustomers();

        // then
        verify(customerRepository, times(1)).findAll();
        assertThat(result.get(0).email()).isEqualTo("alicia@gmail.com");
        assertThat(result.get(1).email()).isEqualTo("david@gmail.com");
    }

    @Test
    void shouldNotGetAnyCustomers() {
        // given
        List<Customer> customers = List.of();

        // when
        when(customerRepository.findAll()).thenReturn(customers);
        List<CustomerDTO> result = customerService.getCustomers();

        // then
        verify(customerRepository, times(1)).findAll();
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    void shouldGetOneCustomer() {
        // given
        Customer customer = new Customer(
                1L,
                "alice@gmail.com",
                "1234"
        );

        // when
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        Customer storedCustomer = customerService.getCustomer(customer.getId());

        // then
        verify(customerRepository, times(1)).findById(customer.getId());
        assertEquals(customer.getId(), storedCustomer.getId());
        assertEquals("alice@gmail.com", storedCustomer.getEmail());
    }

    @Test
    void shouldNotGetAnyCustomerWhenNotExistAndWillThrow() {
        // given
        Long id = -1L;

        // when
        CustomerException exception = assertThrows(CustomerException.class,
                () -> customerService.getCustomer(id)
        );

        // then
        assertEquals("Customer not found.", exception.getMessage());
    }

    @Test
    void shouldCreateCustomer() {
        // given
        final String passwordHash = "¢5554ml;f;lsd";
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "david@gmail.com",
                "123456",
                "david",
                "white",
                "123 123 123",
                LocalDate.of(1989, 1, 1),
                CustomerGender.MALE,
                "",
                "Fake AV",
                "50120",
                "USA",
                "123123123Z"
        );

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
                LocalDate.of(1989, 1, 1),
                CustomerGender.MALE,
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
        // given
        final String currentRawPassword = "123456";
        final String currentEncodedPassword = this.hashedPassword;
        final String rawNewPassword = "1234";
        final String encodedNewPassword = "encodedNewPassword";

        Customer customer = new Customer(
                10L,
                "david@gmail.com",
                currentEncodedPassword
        );

        // we change the context customer
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                customer, null, Collections.emptyList()));

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                "david@test.com",
                currentRawPassword,
                rawNewPassword
        );

        // when
        when(bCryptPasswordEncoder.encode(rawNewPassword)).thenReturn(encodedNewPassword);
        when(bCryptPasswordEncoder.matches(currentRawPassword, currentEncodedPassword)).thenReturn(true);
        when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        customerService.updateCustomer(updateRequest);

        // then
        verify(customerRepository, times(1)).save(customer);
        assertThat(customer.getEmail()).isEqualTo(updateRequest.newEmail());
        assertThat(customer.getPassword()).isEqualTo(encodedNewPassword);
    }

    @Test
    void shouldNotUpdatePasswordWhenCurrentPasswordDoesNotMatch() {
        // Given
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                customer.getEmail(),
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
        // given
        String currentRawPassword = "123456";
        String currentEncodedPassword = "encodedActualPassword";

        Customer customer = new Customer(
                10L,
                "david@gmail.com",
                currentEncodedPassword
        );

        // we change the context customer
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                customer, null, Collections.emptyList()));

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                "david@gmail.com",
                currentRawPassword,
                null
        );

        // when
        when(bCryptPasswordEncoder.matches(currentRawPassword, currentEncodedPassword)).thenReturn(true);
        when(customerRepository.findByEmail(updateRequest.newEmail())).thenReturn(Optional.of(customer));

        customerService.updateCustomer(updateRequest);

        // then
        verify(customerRepository, times(1)).save(customer);
        assertThat(customer.getEmail()).isEqualTo(updateRequest.newEmail());
        assertThat(customer.getPassword()).isEqualTo(currentEncodedPassword);
    }
}
