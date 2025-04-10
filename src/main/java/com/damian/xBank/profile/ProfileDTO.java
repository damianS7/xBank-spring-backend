package com.damian.xBank.profile;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public record ProfileDTO(
        Long id,
        String name,
        String surname,
        String phone,
        String birthdate,
        @Enumerated(EnumType.STRING)
        Gender gender,
        String photo,
        String address,
        String postalCode,
        String country,
        String nationalId) {
}
