package com.damian.xBank.banking.card.exception;

import com.damian.xBank.auth.exception.AuthorizationException;

public class BankingCardAuthorizationException extends AuthorizationException {
    public BankingCardAuthorizationException(String message) {
        super(message);
    }

    public BankingCardAuthorizationException() {
        this("You are not the owner of this card.");
    }
}
