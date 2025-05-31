package com.damian.xBank.banking.transactions.http;

import com.damian.xBank.banking.transactions.BankingTransactionStatus;

public record BankingTransactionPatchRequest(
        BankingTransactionStatus transactionStatus
) {
}
