package com.damian.xBank.customer.dto;

import com.damian.xBank.customer.CustomerRole;
import com.damian.xBank.customer.profile.ProfileDTO;

import java.time.Instant;

public record CustomerWithProfileDTO(
        Long id,
        String email,
        CustomerRole role,
        ProfileDTO profile,
        Instant createdAt,
        Instant updatedAt
) {
}