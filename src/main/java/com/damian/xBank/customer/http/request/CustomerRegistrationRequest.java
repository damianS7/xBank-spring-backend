package com.damian.xBank.customer.http.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record CustomerRegistrationRequest(
        @NotNull(message = "Email cannot be empty.")
        @Email(message = "Invalid email.")
        String email,

        @NotNull(message = "Password cannot be empty.")
        String password
) {
}
