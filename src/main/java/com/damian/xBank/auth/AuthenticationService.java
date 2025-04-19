package com.damian.xBank.auth;

import com.damian.xBank.auth.exception.AuthenticationException;
import com.damian.xBank.auth.http.request.AuthenticationRequest;
import com.damian.xBank.auth.http.request.AuthenticationResponse;
import com.damian.xBank.common.utils.JWTUtil;
import com.damian.xBank.customer.Customer;
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

    public AuthenticationService(JWTUtil jwtUtil, AuthenticationManager authenticationManager, CustomerService customerService) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.customerService = customerService;
    }

    public Customer register(CustomerRegistrationRequest request) {
        return customerService.createCustomer(request);
    }

    /**
     * It controls the login flow
     *
     * @param request Contains the fields needed to login into the service
     * @return Contains the data (Customer, Profile) and the token
     * @throws AuthenticationException
     */
    public AuthenticationResponse login(AuthenticationRequest request) {
        final String email = request.email();
        final String password = request.password();

        Authentication auth;

        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email, password)
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new AuthenticationException("Bad credentials"); // 403 Forbidden
        }

        // Token generation
        final String token = jwtUtil.generateToken(email);

        // id from the authenticated customer
        Long customerId = ((Customer) auth.getPrincipal()).getId();

        // fetch the customer logged from the service
        Customer customer = customerService.getCustomer(customerId);

        // Enviamos al usuario de vuelta los datos necesarios para el cliente
        return new AuthenticationResponse(
                customer.toDTO(), token
        );
    }


}
