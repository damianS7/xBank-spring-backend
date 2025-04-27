package com.damian.xBank.banking.transactions;

import java.math.BigDecimal;
import java.time.Instant;

public record BankingTransactionDTO(
        Long id,
        BigDecimal amount,
        BankingTransactionType transactionType,
        BankingTransactionStatus transactionStatus,
        String description,
        Instant date
) {
}
