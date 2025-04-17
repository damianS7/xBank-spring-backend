package com.damian.xBank.profile;

import java.time.LocalDate;

public record ProfileDTO(
        Long id,
        String name,
        String surname,
        String phone,
        LocalDate birthdate,
        Gender gender,
        String photo,
        String address,
        String postalCode,
        String country,
        String nationalId) {
}
