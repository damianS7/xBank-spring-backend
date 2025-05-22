package com.damian.xBank.auth.admin;

import com.damian.xBank.auth.AuthenticationService;
import com.damian.xBank.customer.http.request.CustomerPasswordUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
public class AuthenticationAdminController {

    private final AuthenticationService authenticationService;

    public AuthenticationAdminController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // endpoint to modify customer password
    // FIXME /admin/auth/customers/me/password or /auth/admin/customers/{id}/password/reset???
    @PatchMapping("/admin/auth/customers/{id}/password")
    public ResponseEntity<?> updateCustomerPassword(
            @Validated @RequestBody
            CustomerPasswordUpdateRequest request
    ) {
        authenticationService.updatePassword(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Password updated");
    }
}