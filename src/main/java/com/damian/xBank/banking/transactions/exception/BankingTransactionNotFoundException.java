package com.damian.xBank.banking.transactions.exception;

public class BankingTransactionNotFoundException extends BankingTransactionException {
    public static final String TRANSACTION_NOT_FOUND = "Transaction not found.";

    public BankingTransactionNotFoundException(String message) {
        super(message);
    }
}
