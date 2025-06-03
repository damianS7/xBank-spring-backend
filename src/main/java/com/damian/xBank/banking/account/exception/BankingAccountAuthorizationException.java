package com.damian.xBank.banking.account.exception;

import com.damian.xBank.auth.exception.AuthorizationException;

public class BankingAccountAuthorizationException extends AuthorizationException {
    public BankingAccountAuthorizationException(String message) {
        super(message);
    }

}
