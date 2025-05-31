package com.damian.xBank.banking.transactions.http;

import com.damian.xBank.banking.transactions.BankingTransactionType;

import java.math.BigDecimal;

public record BankingCardTransactionRequest(
        BankingTransactionType transactionType,

        String description,

        BigDecimal amount,
        String cardPin
) {
}
