package com.damian.xBank.banking.card.http;

import com.damian.xBank.banking.transactions.BankingTransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BankingCardSpendRequest(
        // commerce

        @NotNull(message = "You must set an amount for this operation")
        @Positive
        BigDecimal amount,

        @NotNull(message = "You must specify the type for this operation")
        BankingTransactionType transactionType,

        @NotNull(message = "You must give a description for this operation")
        @NotBlank(message = "You must give a description for this operation")
        String description,

        @NotNull(message = "Password must not be null")
        String password

        // pin
        // cvv
) {
}
