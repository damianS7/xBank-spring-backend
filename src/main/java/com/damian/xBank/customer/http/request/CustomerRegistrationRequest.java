package com.damian.xBank.customer.http.request;

import com.damian.xBank.profile.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Contains all the data required for Customer registration
 */
public record CustomerRegistrationRequest(
        @NotNull
        @NotBlank
        @Email
        String email,

        @NotNull
        @NotBlank
        String password,

        @NotNull
        @NotBlank
        String name,

        @NotNull
        @NotBlank
        String surname,

        @NotNull
        @NotBlank
        String phone,

        @NotNull
        @NotBlank
        String birthdate,

        @NotNull
        Gender gender,

        String photo,

        @NotNull
        @NotBlank
        String address,

        @NotNull
        @NotBlank
        String postalCode,

        @NotNull
        @NotBlank
        String country,

        @NotNull
        @NotBlank
        String nationalId
) {
}
