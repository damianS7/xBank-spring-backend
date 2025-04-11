package com.damian.xBank.profile;

public record ProfileDTO(
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
        String nationalId) {
}
