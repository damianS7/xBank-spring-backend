package com.damian.xBank.profile;

import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.NonNull;

public record ProfileUpdateRequest(
        @NonNull
        Long id,
        String name,
        String surname,
        String phone,
        String birthdate,
        Gender gender,
        String photo,
        String address,
        String postalCode,
        String country,
        String nationalId,
        @NonNull
        Long customerId,
        @NonNull
        @NotBlank
        String currentPassword) {
}
