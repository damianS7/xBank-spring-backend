package com.damian.xBank.auth.http;

import com.damian.xBank.profile.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * Plantilla para los datos que debe enviar el usuario en su peticion.
 */
public record CustomerRegistrationRequest(
        @NotNull(message = "Email cannot be empty.")
        @Email(message = "Email format is invalid.")
        String email,

        @NotNull(message = "Password cannot be empty.")
        String password,

        @NotNull(message = "Name cannot be empty.")
        String name,

        @NotNull(message = "Surname cannot be empty.")
        String surname,

        @NotNull(message = "Phone cannot be empty.")
        String phone,

        @NotNull(message = "Birthdate cannot be empty.")
        String birthdate,

        @NotNull(message = "Gender cannot be empty.")
        Gender gender,

        String photo,

        @NotNull(message = "Address cannot be empty.")
        String address,

        @NotNull(message = "Postal code cannot be empty.")
        String postalCode,

        @NotNull(message = "Country cannot be empty.")
        String country,

        @NotNull(message = "National ID cannot be empty.")
        String nationalId
) {
}
