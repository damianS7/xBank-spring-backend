package com.damian.xBank.banking.transactions.exception;

import com.damian.xBank.auth.exception.AuthorizationException;

public class BankingTransactionAuthorizationException extends AuthorizationException {
    public BankingTransactionAuthorizationException(String message) {
        super(message);
    }
}
