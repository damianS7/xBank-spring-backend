package com.damian.xBank.profile.http;

import com.damian.xBank.profile.Gender;
import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.NonNull;

import java.time.LocalDate;

public record ProfileUpdateRequest(
        @NonNull
        Long id,

        @NotBlank
        String name,

        @NotBlank
        String surname,

        @NotBlank
        String phone,

        @NotBlank
        LocalDate birthdate,

        @NonNull
        Gender gender,

        @NotBlank
        String photo,

        @NotBlank
        String address,

        @NotBlank
        String postalCode,

        @NotBlank
        String country,

        @NotBlank
        String nationalId,

        @NonNull
        Long customerId,

        @NotBlank
        String currentPassword) {
}
