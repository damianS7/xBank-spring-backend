package com.damian.xBank.banking.account.exception;

import com.damian.xBank.auth.exception.AuthorizationException;

public class BankingAccountAuthorizationException extends AuthorizationException {
    public BankingAccountAuthorizationException(String message) {
        super(message);
    }

    public BankingAccountAuthorizationException(Long id) {
        this("You are not the owner of this account. Account id: " + id);
    }

    public BankingAccountAuthorizationException() {
        this("You are not the owner of this account.");
    }
}
