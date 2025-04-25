package com.damian.xBank.customer;

import com.damian.xBank.customer.exception.CustomerEmailTakenException;
import com.damian.xBank.customer.exception.CustomerNotFoundException;
import com.damian.xBank.customer.http.request.CustomerEmailUpdateRequest;
import com.damian.xBank.customer.http.request.CustomerRegistrationRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        customerRepository.deleteAll();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    void setUpContext(Customer customer) {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(customer);
    }

    @Test
    @DisplayName("Should get all customers")
    void shouldGetAllCustomers() {
        // given

        List<Customer> customers = List.of(
                new Customer(1L, "customer1@test.com", "123455"),
                new Customer(2L, "customer2@test.com", "123456")
        );

        // when
        when(customerRepository.findAll()).thenReturn(customers);

        List<CustomerDTO> result = customerService.getCustomers();

        // then
        verify(customerRepository, times(1)).findAll();
        assertThat(result.get(0).email()).isEqualTo("customer1@test.com");
        assertThat(result.get(1).email()).isEqualTo("customer2@test.com");
    }

    @Test
    @DisplayName("Should not get any customers")
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
    @DisplayName("Should find customer")
    void shouldFindCustomer() {
        // given
        Customer customer = new Customer(
                1L,
                "customer@test.com",
                "1234"
        );

        // when
        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        Customer storedCustomer = customerService.getCustomer(customer.getId());

        // then
        verify(customerRepository, times(1)).findById(customer.getId());
        assertEquals(customer.getId(), storedCustomer.getId());
        assertEquals(customer.getEmail(), storedCustomer.getEmail());
    }

    @Test
    @DisplayName("Should not find customer when not exist")
    void shouldNotFindCustomerWhenNotExist() {
        // given
        Long id = -1L;

        // when
        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class,
                () -> customerService.getCustomer(id)
        );

        // then
        assertTrue(exception.getMessage().contains("Customer not found"));
    }

    @Test
    @DisplayName("Should create customer")
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
    @DisplayName("Should not create any customer when email is taken")
    void shouldNotCreateAnyCustomerWhenEmailIsTaken() {
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
        CustomerEmailTakenException exception = assertThrows(CustomerEmailTakenException.class,
                () -> customerService.createCustomer(request)
        );

        // then
        verify(customerRepository, times(0)).save(any());
        assertTrue(exception.getMessage().contains("is already taken."));
    }

    @Test
    @DisplayName("Should delete customer")
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
    @DisplayName("Should not delete customer when not exist")
    void shouldNotDeleteCustomerWhenNotExist() {
        // given
        Long id = -1L;

        // when
        when(customerRepository.existsById(id)).thenReturn(false);

        // then
        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class,
                () -> customerService.deleteCustomer(id)
        );
        assertTrue(exception.getMessage().contains("Customer not found"));
        verify(customerRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should update customer email")
    void shouldUpdateCustomerEmail() {
        // given
        String currentRawPassword = "123456";
        String currentEncodedPassword = "encodedActualPassword";

        Customer customer = new Customer(
                10L,
                "customer@test.com",
                currentEncodedPassword
        );

        // set the customer on the context
        setUpContext(customer);

        CustomerEmailUpdateRequest updateRequest = new CustomerEmailUpdateRequest(
                currentRawPassword,
                "david@test.com"
        );

        // when
        when(bCryptPasswordEncoder.matches(currentRawPassword, currentEncodedPassword)).thenReturn(true);
        when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));

        customerService.updateEmail(updateRequest);

        // then
        verify(customerRepository, times(1)).save(customer);
        assertThat(customer.getEmail()).isEqualTo(updateRequest.newEmail());
        assertThat(customer.getPassword()).isEqualTo(currentEncodedPassword);
    }
}
