package com.damian.xBank.banking.account.transactions;

import java.math.BigDecimal;
import java.time.Instant;

public record BankingAccountTransactionDTO(
        Long id,
        BigDecimal amount,
        BankingAccountTransactionType transactionType,
        BankingAccountTransactionStatus transactionStatus,
        String description,
        Instant date
) {
}
