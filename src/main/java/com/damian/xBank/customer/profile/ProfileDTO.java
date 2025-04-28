package com.damian.xBank.customer.profile;

import com.damian.xBank.customer.CustomerGender;

import java.time.Instant;
import java.time.LocalDate;

public record ProfileDTO(
        Long id,
        String name,
        String surname,
        String phone,
        LocalDate birthdate,
        CustomerGender gender,
        String photoPath,
        String address,
        String postalCode,
        String country,
        String nationalId,
        Instant updatedAt
) {
}
