package com.damian.xBank.banking.account.exception;

public class BankingAccountNotFoundException extends BankingAccountException {
    public BankingAccountNotFoundException(String message) {
        super(message);
    }

    public BankingAccountNotFoundException() {
        this("Banking account not found.");
    }

    public BankingAccountNotFoundException(Long id) {
        this("Banking account not found with id: " + id);
    }
}
