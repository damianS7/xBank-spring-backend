package com.damian.xBank.banking.account;

import java.math.BigDecimal;
import java.time.Instant;

public record BankingAccountDTO(
        Long id,
        String number,
        BigDecimal balance,
        BankingAccountType type,
        BankingAccountCurrency currency,
        BankingAccountStatus status,
        Instant updatedAt
) {
}
