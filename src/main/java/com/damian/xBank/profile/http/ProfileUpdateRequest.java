package com.damian.xBank.profile.http;

import com.damian.xBank.profile.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

        @NotNull
        LocalDate birthdate,

        @NonNull
        Gender gender,

        @NotBlank
        String photoPath,

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
