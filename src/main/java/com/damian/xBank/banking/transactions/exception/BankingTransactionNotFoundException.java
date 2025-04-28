package com.damian.xBank.banking.transactions.exception;

public class BankingTransactionNotFoundException extends BankingTransactionException {
    public BankingTransactionNotFoundException(String message) {
        super(message);
    }

    public BankingTransactionNotFoundException() {
        this("Banking transaction not found.");
    }

    public BankingTransactionNotFoundException(Long id) {
        this("Banking transaction not found with id: " + id);
    }
}
