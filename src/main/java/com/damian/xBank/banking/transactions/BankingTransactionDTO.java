package com.damian.xBank.banking.transactions;

import java.math.BigDecimal;
import java.time.Instant;

public record BankingTransactionDTO(
        Long id,
        Long bankingAccountId,
        Long bankingCardId,
        BigDecimal amount,
        BankingTransactionType transactionType,
        BankingTransactionStatus transactionStatus,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
