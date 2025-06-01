package com.damian.xBank.banking.account.exception;

import com.damian.xBank.auth.exception.AuthorizationException;

public class BankingAccountAuthorizationException extends AuthorizationException {
    public static final String ACCOUNT_NOT_BELONG_TO_CUSTOMER = "This account does not belong to the customer.";

    public BankingAccountAuthorizationException(String message) {
        super(message);
    }

}
