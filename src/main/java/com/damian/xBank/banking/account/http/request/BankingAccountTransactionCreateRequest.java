package com.damian.xBank.banking.account.http.request;

import com.damian.xBank.banking.transactions.BankingTransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BankingAccountTransactionCreateRequest(
        // the account that receives the money in case its a TRANSFER
        @Positive
        Long toBankingAccountId,

        @NotNull(message = "You must set an amount for this operation")
        @Positive
        BigDecimal amount,

        @NotNull(message = "You must specify the type for this operation")
        BankingTransactionType transactionType,

        @NotNull(message = "You must give a description for this operation")
        @NotBlank(message = "You must give a description for this operation")
        String description
) {
}
