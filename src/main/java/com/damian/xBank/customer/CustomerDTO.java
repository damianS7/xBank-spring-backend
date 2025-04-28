package com.damian.xBank.customer;

import com.damian.xBank.banking.account.BankingAccountDTO;
import com.damian.xBank.customer.profile.ProfileDTO;

import java.time.Instant;
import java.util.Set;

public record CustomerDTO(
        Long id,
        String email,
        CustomerRole role,
        ProfileDTO profile,
        Set<BankingAccountDTO> bankingAccounts,
        Instant createdAt,
        Instant updatedAt
) {
}