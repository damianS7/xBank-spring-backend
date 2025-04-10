package com.damian.xBank.customer;

import com.damian.xBank.profile.ProfileDTO;

public record CustomerDTO(
        Long id,
        String email,
        CustomerRole role,
        ProfileDTO profile) {


}