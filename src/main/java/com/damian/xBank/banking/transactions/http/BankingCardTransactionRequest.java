package com.damian.xBank.banking.transactions.http;

import com.damian.xBank.banking.transactions.BankingTransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BankingCardTransactionRequest(
        @NotNull(message = "You must specify the type for this operation")
        BankingTransactionType transactionType,

        @NotNull(message = "You must give a description for this operation")
        String description,

        @Positive
        BigDecimal amount,

        @NotNull(message = "You must input card pid to confirm this operation")
        String cardPin
) {
}
