package com.damian.xBank.auth.http;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * Plantilla para los datos que debe enviar el usuario en su peticion.
 */
public record AuthenticationRequest(
        @NotNull(message = "Email cannot be empty.")
        @Email(message = "Invalid email.")
        String email,

        @NotNull(message = "Password cannot be empty.")
        String password) {
}
