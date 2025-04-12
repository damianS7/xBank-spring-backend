package com.damian.xBank.customer.http.request;

import com.damian.xBank.profile.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Contains all the data required for Customer registration
 */
public record CustomerRegistrationRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must be at least 8 characters long, contain at least one uppercase letter, " +
                        "one number, and one special character.")
        String password,

        @NotBlank
        String name,

        @NotBlank
        String surname,

        @NotBlank
        String phone,

        @NotBlank
        String birthdate,

        @NotNull
        Gender gender,

        String photo,

        @NotBlank
        String address,

        @NotBlank
        String postalCode,

        @NotBlank
        String country,

        @NotBlank
        String nationalId
) {
}
