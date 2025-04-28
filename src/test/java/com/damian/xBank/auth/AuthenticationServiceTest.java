package com.damian.xBank.auth;

import com.damian.xBank.auth.exception.AuthenticationAccountDisabledException;
import com.damian.xBank.auth.exception.AuthenticationBadCredentialsException;
import com.damian.xBank.auth.http.AuthenticationRequest;
import com.damian.xBank.auth.http.AuthenticationResponse;
import com.damian.xBank.common.exception.PasswordMismatchException;
import com.damian.xBank.common.utils.JWTUtil;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerGender;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerService;
import com.damian.xBank.customer.exception.CustomerNotFoundException;
import com.damian.xBank.customer.http.request.CustomerPasswordUpdateRequest;
import com.damian.xBank.customer.http.request.CustomerRegistrationRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito en JUnit 5
public class AuthenticationServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AuthenticationRepository authenticationRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private CustomerService customerService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private JWTUtil jwtUtil;
    private String hashedPassword = "3hri2rhid;/!";

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
    @DisplayName("")
    void shouldRegisterCustomer() {
        // given
        Customer givenCustomer = new Customer();
        givenCustomer.setEmail("customer@test.com");
        givenCustomer.setPassword("123456");
        givenCustomer.getProfile().setNationalId("123456789Z");
        givenCustomer.getProfile().setName("John");
        givenCustomer.getProfile().setSurname("Wick");
        givenCustomer.getProfile().setPhone("123 123 123");
        givenCustomer.getProfile().setGender(CustomerGender.MALE);
        givenCustomer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
        givenCustomer.getProfile().setCountry("USA");
        givenCustomer.getProfile().setAddress("fake ave");
        givenCustomer.getProfile().setPostalCode("050012");
        givenCustomer.getProfile().setPhotoPath("no photoPath");

        CustomerRegistrationRequest registrationRequest = new CustomerRegistrationRequest(
                givenCustomer.getEmail(),
                givenCustomer.getPassword(),
                givenCustomer.getProfile().getName(),
                givenCustomer.getProfile().getSurname(),
                givenCustomer.getProfile().getPhone(),
                givenCustomer.getProfile().getBirthdate(),
                givenCustomer.getProfile().getGender(),
                givenCustomer.getProfile().getPhotoPath(),
                givenCustomer.getProfile().getAddress(),
                givenCustomer.getProfile().getPostalCode(),
                givenCustomer.getProfile().getCountry(),
                givenCustomer.getProfile().getNationalId()
        );

        // when
        when(customerService.createCustomer(any(CustomerRegistrationRequest.class))).thenReturn(givenCustomer);
        Customer registeredCustomer = authenticationService.register(registrationRequest);

        // then
        verify(customerService, times(1)).createCustomer(registrationRequest);
        assertThat(registeredCustomer).isNotNull();
        assertThat(registeredCustomer.getEmail()).isEqualTo(givenCustomer.getEmail());
        assertThat(registeredCustomer.getProfile().getName()).isEqualTo(givenCustomer.getProfile().getName());
        assertThat(registeredCustomer.getProfile().getSurname()).isEqualTo(givenCustomer.getProfile().getSurname());
        assertThat(registeredCustomer.getProfile().getPhone()).isEqualTo(givenCustomer.getProfile().getPhone());
        assertThat(registeredCustomer.getProfile().getGender()).isEqualTo(givenCustomer.getProfile().getGender());
        assertThat(registeredCustomer.getProfile().getBirthdate()).isEqualTo(givenCustomer.getProfile().getBirthdate());
        assertThat(registeredCustomer.getProfile().getCountry()).isEqualTo(givenCustomer.getProfile().getCountry());
        assertThat(registeredCustomer.getProfile().getAddress()).isEqualTo(givenCustomer.getProfile().getAddress());
        assertThat(registeredCustomer.getProfile().getPostalCode()).isEqualTo(givenCustomer
                .getProfile()
                .getPostalCode());
        assertThat(registeredCustomer.getProfile().getNationalId()).isEqualTo(givenCustomer
                .getProfile()
                .getNationalId());
    }

    @Test
    void shouldLoginWhenValidCredentials() {
        // given
        Authentication authentication = mock(Authentication.class);
        String token = "jwt-token";

        Customer customer = new Customer(
                1L,
                "alice@gmail.com",
                "123456"
        );

        AuthenticationRequest request = new AuthenticationRequest(customer.getEmail(), customer.getPassword());

        // when
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(request.email())).thenReturn(token);
        when(authentication.getPrincipal()).thenReturn(customer);

        AuthenticationResponse response = authenticationService.login(request);

        // then
        assertThat(response.token()).isEqualTo(token);
        assertThat(customer.getId()).isEqualTo(response.customer().id());
        assertThat(customer.getEmail()).isEqualTo(response.customer().email());
    }

    @Test
    void shouldNotLoginWhenInvalidCredentials() {
        // given
        Customer customer = new Customer(
                1L,
                "alice@gmail.com",
                "123456"
        );

        AuthenticationRequest request = new AuthenticationRequest(customer.getEmail(), customer.getPassword());

        // when
        when(authenticationManager.authenticate(any())).thenThrow(AuthenticationBadCredentialsException.class);

        AuthenticationBadCredentialsException exception = assertThrows(
                AuthenticationBadCredentialsException.class,
                () -> authenticationService.login(request)
        );

        // Then
        assertTrue(exception.getMessage().contains("Bad credentials."));
    }

    @Test
    void shouldNotLoginWhenAccountIsDisabled() {
        // given
        Authentication authentication = mock(Authentication.class);
        String token = "jwt-token";

        Customer customer = new Customer(
                1L,
                "alice@gmail.com",
                "123456"
        );
        customer.getAuth().setAuthAccountStatus(AuthAccountStatus.DISABLED);

        AuthenticationRequest request = new AuthenticationRequest(customer.getEmail(), customer.getPassword());

        // when
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(request.email())).thenReturn(token);
        when(authentication.getPrincipal()).thenReturn(customer);

        AuthenticationAccountDisabledException exception = assertThrows(
                AuthenticationAccountDisabledException.class,
                () -> authenticationService.login(request)
        );

        // Then
        assertTrue(exception.getMessage().contains("Account is disabled."));
    }

    @Test
    @DisplayName("Should update customer password")
    void shouldUpdateCustomerPassword() {
        // given
        final String currentRawPassword = "123456";
        final String currentEncodedPassword = this.hashedPassword;
        final String rawNewPassword = "1234";
        final String encodedNewPassword = "encodedNewPassword";

        Customer customer = new Customer(
                10L,
                "customer@test.com",
                currentEncodedPassword
        );

        CustomerPasswordUpdateRequest updateRequest = new CustomerPasswordUpdateRequest(
                currentRawPassword,
                rawNewPassword
        );

        // set the customer on the context
        setUpContext(customer);

        // when
        when(bCryptPasswordEncoder.encode(rawNewPassword)).thenReturn(encodedNewPassword);
        when(bCryptPasswordEncoder.matches(currentRawPassword, currentEncodedPassword)).thenReturn(true);
        when(authenticationRepository.findByCustomer_Id(customer.getId())).thenReturn(Optional.of(customer.getAuth()));
        authenticationService.updatePassword(updateRequest);

        // then
        verify(authenticationRepository, times(1)).save(customer.getAuth());
        assertThat(customer.getPassword()).isEqualTo(encodedNewPassword);
    }

    @Test
    @DisplayName("Should not update password when current password does not match")
    void shouldNotUpdatePasswordWhenCurrentPasswordDoesNotMatch() {
        // given
        Customer customer = new Customer(
                10L,
                "customer@test.com",
                "currentEncodedPassword"
        );

        // set the customer on the context
        setUpContext(customer);

        CustomerPasswordUpdateRequest updateRequest = new CustomerPasswordUpdateRequest(
                "wrongPassword",
                "1234"
        );

        // when
        when(authenticationRepository.findByCustomer_Id(customer.getId())).thenReturn(Optional.of(customer.getAuth()));
        when(bCryptPasswordEncoder.matches(updateRequest.currentPassword(), customer.getPassword())).thenReturn(false);
        PasswordMismatchException exception = assertThrows(
                PasswordMismatchException.class,
                () -> authenticationService.updatePassword(
                        updateRequest
                )
        );
        // Then
        assertTrue(exception.getMessage().contains("Password does not match."));
    }

    @Test
    @DisplayName("Should not update password when auth entity not found")
    void shouldNotUpdatePasswordWhenAuthNotFound() {
        // given
        Customer customer = new Customer(
                10L,
                "customer@test.com",
                "encodedNewPassword"
        );

        // set the customer on the context
        setUpContext(customer);

        CustomerPasswordUpdateRequest updateRequest = new CustomerPasswordUpdateRequest(
                "1234",
                "1234"
        );

        // when
        when(authenticationRepository.findByCustomer_Id(customer.getId()))
                .thenReturn(Optional.empty());


        CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> authenticationService.updatePassword(
                        updateRequest
                )
        );

        // Then
        assertTrue(exception.getMessage().contains("Customer not found"));
    }
}
