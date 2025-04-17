package com.damian.xBank.auth.http.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request with required fields for login
 */
public record AuthenticationRequest(
        @NotNull
        @NotBlank
        @Email
        String email,

        @NotNull
        @NotBlank
        String password) {
}
