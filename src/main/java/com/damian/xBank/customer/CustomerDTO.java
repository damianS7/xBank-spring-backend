package com.damian.xBank.customer;

import com.damian.xBank.banking.account.BankingAccountDTO;
import com.damian.xBank.profile.ProfileDTO;

import java.util.Set;

public record CustomerDTO(
        Long id,
        String email,
        CustomerRole role,
        ProfileDTO profile,
        Set<BankingAccountDTO> bankingAccounts
) {
}