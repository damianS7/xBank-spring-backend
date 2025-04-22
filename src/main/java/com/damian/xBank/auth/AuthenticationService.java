package com.damian.xBank.auth;

import com.damian.xBank.auth.exception.AuthenticationException;
import com.damian.xBank.auth.http.request.AuthenticationRequest;
import com.damian.xBank.auth.http.request.AuthenticationResponse;
import com.damian.xBank.common.utils.JWTUtil;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerService;
import com.damian.xBank.customer.http.request.CustomerRegistrationRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    public AuthenticationService(JWTUtil jwtUtil, AuthenticationManager authenticationManager, CustomerService customerService, CustomerRepository customerRepository) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
    }

    /**
     * Register a new customer.
     *
     * @param request Contains the fields needed for the customer creation
     * @return The customer created
     */
    public Customer register(CustomerRegistrationRequest request) {
        // It uses the customer service to create a new customer
        return customerService.createCustomer(request);
    }

    /**
     * Controls the login flow.
     *
     * @param request Contains the fields needed to login into the service
     * @return Contains the data (Customer, Profile) and the token
     * @throws AuthenticationException
     */
    public AuthenticationResponse login(AuthenticationRequest request) {
        final String email = request.email();
        final String password = request.password();

        // Authenticate the user
        Authentication auth;

        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email, password)
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new AuthenticationException(e.getMessage()); // 403 Forbidden
        }

        // Generate a token for the authenticated user
        final String token = jwtUtil.generateToken(email);

        // Get the id from the authenticated customer
        Long customerId = ((Customer) auth.getPrincipal()).getId();

        // Fetch the customer logged from the service
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AuthenticationException("Customer not found.")
                );

        // Return the customer data and the token
        return new AuthenticationResponse(
                customer.toDTO(), token
        );
    }


}
