package com.damian.xBank.banking.account.exception;

public class BankingAccountNotFoundException extends BankingAccountException {
    public static final String ACCOUNT_NOT_FOUND = "Account not found.";

    public BankingAccountNotFoundException(String message) {
        super(message);
    }
}
