package com.damian.xBank.auth;

import com.damian.xBank.auth.http.AuthenticationRequest;
import com.damian.xBank.auth.http.AuthenticationResponse;
import com.damian.xBank.common.http.response.ApiResponse;
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
        ApiResponse<?> response = ApiResponse.success(
                customer.toDTO(),
                HttpStatus.CREATED
        );

        return ResponseEntity.status(response.getHttpCode()).body(response);
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse authResponse = authenticationService.login(request);

        ApiResponse<?> response = ApiResponse.success(
                authResponse,
                HttpStatus.OK
        );

        return ResponseEntity
                .status(response.getHttpCode())
                .header(HttpHeaders.AUTHORIZATION, authResponse.token())
                .body(response);
    }

}