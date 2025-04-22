package com.damian.xBank.customer.profile.http.request;

import com.damian.xBank.customer.CustomerGender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ProfileUpdateRequest(
        @NotBlank(message = "Name must not be blank.")
        String name,

        @NotBlank(message = "Surname must not be blank.")
        String surname,

        @NotBlank(message = "Phone must not be blank.")
        String phone,

        @NotNull(message = "Birthdate must not be null.")
        LocalDate birthdate,

        @NotNull(message = "Gender must not be null")
        CustomerGender gender,

        String photoPath,

        @NotBlank(message = "Address must not be blank.")
        String address,

        @NotBlank(message = "Postal code must not be blank.")
        String postalCode,

        @NotBlank(message = "Country must not be blank.")
        String country,

        @NotBlank(message = "National ID must not be blank.")
        String nationalId,

        @NotBlank
        String currentPassword) {
}
