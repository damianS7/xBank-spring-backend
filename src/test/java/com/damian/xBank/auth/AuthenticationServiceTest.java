package com.damian.xBank.auth;

import com.damian.xBank.auth.http.AuthenticationRequest;
import com.damian.xBank.auth.http.AuthenticationResponse;
import com.damian.xBank.customer.*;
import com.damian.xBank.utils.JWTUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito en JUnit 5
public class AuthenticationServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private CustomerService customerService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, bCryptPasswordEncoder);
        authenticationService = new AuthenticationService(
                mock(JWTUtil.class), mock(AuthenticationManager.class)
        );
        customerRepository.deleteAll();
    }

    @Test
    void shouldLoginWhenValidCredentials() {
        // given
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        String token = "mocked-jwt-token";

        AuthenticationRequest request = new AuthenticationRequest(email, password);
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setEmail(email);
        customer.setRole(CustomerRole.CUSTOMER);

        Authentication authentication = new UsernamePasswordAuthenticationToken(customer, null);

        // when

//        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        AuthenticationResponse response = authenticationService.login(request);
//        when(authenticationService.auth(request)).thenReturn(authentication);
//        when(jwtUtil.generateToken(email)).thenReturn(token);


        // then
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.token()).isEqualTo(token);


    }

}
