package com.damian.xBank.auth;

import com.damian.xBank.auth.http.AuthenticationRequest;
import com.damian.xBank.auth.http.AuthenticationResponse;
import com.damian.xBank.customer.Customer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // endpoint para nuevos usuarios (registro)
    @PostMapping("register")
    public ResponseEntity<?> register(@Validated @RequestBody AuthenticationRequest request) {
        Customer customer = authenticationService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customer.toDTO());
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@Validated @RequestBody AuthenticationRequest request) {
        AuthenticationResponse authResponse = authenticationService.login(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.AUTHORIZATION, authResponse.token())
                .body(authResponse);
    }

}