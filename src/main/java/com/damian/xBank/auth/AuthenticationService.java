package com.damian.xBank.auth;

import com.damian.xBank.auth.exception.AuthenticationAccountDisabledException;
import com.damian.xBank.auth.exception.AuthenticationBadCredentialsException;
import com.damian.xBank.auth.http.AuthenticationRequest;
import com.damian.xBank.auth.http.AuthenticationResponse;
import com.damian.xBank.common.exception.PasswordMismatchException;
import com.damian.xBank.common.utils.JWTUtil;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerService;
import com.damian.xBank.customer.exception.CustomerNotFoundException;
import com.damian.xBank.customer.http.request.CustomerPasswordUpdateRequest;
import com.damian.xBank.customer.http.request.CustomerRegistrationRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthenticationService {
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomerService customerService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationRepository authenticationRepository;

    public AuthenticationService(
            JWTUtil jwtUtil,
            AuthenticationManager authenticationManager,
            CustomerService customerService,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            AuthenticationRepository authenticationRepository
    ) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.customerService = customerService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationRepository = authenticationRepository;
    }

    /**
     * Register a new customer.
     *
     * @param request Contains the fields needed for the customer creation
     * @return The customer created
     */
    public Customer register(CustomerRegistrationRequest request) {
        // It uses the customer service to create a new customer
        Customer registeredCustomer = customerService.createCustomer(request);

        // send welcome email
        // Generate token for email validation
        // send email to confirm registration

        return registeredCustomer;
    }

    /**
     * Controls the login flow.
     *
     * @param request Contains the fields needed to login into the service
     * @return Contains the data (Customer, Profile) and the token
     * @throws AuthenticationBadCredentialsException  if credentials are invalid
     * @throws AuthenticationAccountDisabledException if the account is not enabled
     */
    public AuthenticationResponse login(AuthenticationRequest request) {
        final String email = request.email();
        final String password = request.password();
        final Authentication auth;

        try {
            // Authenticate the user
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email, password)
            );
        } catch (BadCredentialsException e) {
            throw new AuthenticationBadCredentialsException();
        }

        // Generate a token for the authenticated user
        final String token = jwtUtil.generateToken(email);

        // Get the authenticated user
        final Customer customer = (Customer) auth.getPrincipal();

        // check if the account is disabled
        if (customer.getAuth().getAuthAccountStatus().equals(AuthAccountStatus.DISABLED)) {
            throw new AuthenticationAccountDisabledException("Account is disabled.");
        }

        // Return the customer data and the token
        return new AuthenticationResponse(
                token
        );
    }

    /**
     * It updates the password of a customer
     *
     * @param request the request body that contains the current password and the new password
     * @return the customer updated
     * @throws CustomerNotFoundException if the customer does not exist
     * @throws PasswordMismatchException if the password does not match
     */
    public void updatePassword(CustomerPasswordUpdateRequest request) {
        // we extract the email from the Customer stored in the SecurityContext
        final Customer loggedCustomer = (Customer) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // we get the Customer entity so we can save at the end
        Auth customerAuth = authenticationRepository.findByCustomer_Id(loggedCustomer.getId()).orElseThrow(
                () -> new CustomerNotFoundException(loggedCustomer.getId())
        );

        // Before making any changes we check that the password sent by the customer matches the one in the entity
        if (!bCryptPasswordEncoder.matches(request.currentPassword(), customerAuth.getPassword())) {
            throw new PasswordMismatchException("Password does not match.");
        }

        // if a new password is specified we set in the customer entity
        if (request.newPassword() != null) {
            customerAuth.setPassword(
                    bCryptPasswordEncoder.encode(request.newPassword())
            );
        }

        // we change the updateAt timestamp field
        customerAuth.setUpdatedAt(Instant.now());

        // save the changes
        authenticationRepository.save(customerAuth);
    }
}
