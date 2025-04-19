package com.damian.xBank.auth;

import com.damian.xBank.auth.http.request.AuthenticationRequest;
import com.damian.xBank.auth.http.request.AuthenticationResponse;
import com.damian.xBank.common.utils.JWTUtil;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Habilita Mockito en JUnit 5
public class AuthenticationServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private CustomerService customerService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTUtil jwtUtil;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
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
        when(customerService.getCustomer(customer.getId())).thenReturn(customer);

        AuthenticationResponse response = authenticationService.login(request);

        // then
        assertThat(response.token()).isEqualTo(token);
        assertThat(customer.getId()).isEqualTo(response.customer().id());
        assertThat(customer.getEmail()).isEqualTo(response.customer().email());
    }
}
