package com.damian.xBank.banking.account.exception;

import com.damian.xBank.auth.exception.AuthorizationException;

public class BankingAccountAuthorizationException extends AuthorizationException {
    public BankingAccountAuthorizationException(String message) {
        super(message);
    }

    public BankingAccountAuthorizationException() {
        this("You are not the owner of this account.");
    }
}
