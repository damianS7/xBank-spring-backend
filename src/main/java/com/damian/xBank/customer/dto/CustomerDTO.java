package com.damian.xBank.customer.dto;

import com.damian.xBank.customer.CustomerRole;

import java.time.Instant;

public record CustomerDTO(
        Long id,
        String email,
        CustomerRole role,
        Instant createdAt,
        Instant updatedAt
) {
}