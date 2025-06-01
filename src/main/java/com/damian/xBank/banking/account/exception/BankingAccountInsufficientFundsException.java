package com.damian.xBank.banking.account.exception;

public class BankingAccountInsufficientFundsException extends BankingAccountException {
    public static final String INSUFFICIENT_FUNDS = "Insufficient funds.";

    public BankingAccountInsufficientFundsException(String message) {
        super(message);
    }
}
