package com.damian.xBank.auth;

import com.damian.xBank.auth.http.request.AuthenticationRequest;
import com.damian.xBank.auth.http.request.AuthenticationResponse;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.http.request.CustomerPasswordUpdateRequest;
import com.damian.xBank.customer.http.request.CustomerRegistrationRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // endpoint for registration
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(
            @Validated @RequestBody
            CustomerRegistrationRequest request) {
        Customer registeredCustomer = authenticationService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registeredCustomer.toDTO());
    }

    // endpoint for login
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(
            @Validated @RequestBody
            AuthenticationRequest request) {
        AuthenticationResponse authResponse = authenticationService.login(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.AUTHORIZATION, authResponse.token())
                .body(authResponse);
    }

    // endpoint to modify customer password
    @PatchMapping("/auth/customers/password")
    public ResponseEntity<?> updateCustomerPassword(
            @Validated @RequestBody
            CustomerPasswordUpdateRequest request) {
        authenticationService.updatePassword(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Password updated");
    }
}