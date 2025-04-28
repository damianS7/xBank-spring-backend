package com.damian.xBank.banking.transactions;

public record BankingTransactionPatchRequest(
        BankingTransactionStatus transactionStatus
) {
}
