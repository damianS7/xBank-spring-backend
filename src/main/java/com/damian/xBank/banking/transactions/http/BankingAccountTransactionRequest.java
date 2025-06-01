package com.damian.xBank.banking.transactions.http;

import com.damian.xBank.banking.transactions.BankingTransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BankingAccountTransactionRequest(
        String toBankingAccountNumber,

        @NotNull(message = "Transaction type must not be null")
        BankingTransactionType transactionType,

        @NotNull(message = "Description must not be null")
        String description,
        
        @Positive
        BigDecimal amount,

        @NotNull(message = "Password must not be null")
        String password
) {
}
