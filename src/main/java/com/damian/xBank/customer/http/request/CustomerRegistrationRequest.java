package com.damian.xBank.customer.http.request;

import com.damian.xBank.profile.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * Contains all the data required for Customer registration
 */
public record CustomerRegistrationRequest(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a well-formed email address.")
        String email,

        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must be at least 8 characters long, contain at least one uppercase letter, " +
                        "one number, and one special character.")
        String password,

        @NotBlank(message = "Name must not be blank.")
        String name,

        @NotBlank(message = "Surname must not be blank.")
        String surname,

        @NotBlank(message = "Phone must not be blank.")
        String phone,

        @NotNull(message = "Birthdate must not be null.")
        LocalDate birthdate,

        @NotNull
        Gender gender,

        String photo,

        @NotBlank(message = "Address must not be blank.")
        String address,

        @NotBlank(message = "Postal code must not be blank.")
        String postalCode,

        @NotBlank(message = "Country must not be blank.")
        String country,

        @NotBlank(message = "National ID must not be blank.")
        String nationalId
) {
}
