package com.damian.xBank.banking.account.exception;

import com.damian.xBank.auth.exception.AuthorizationException;

public class BankingAccountAuthorizationException extends AuthorizationException {
    public static final String ACCOUNT_NOT_BELONG_TO_CUSTOMER = "This account does not belong to the customer.";
    public static final String ACCOUNT_SUSPENDED = "This account is suspended.";
    public static final String ACCOUNT_CLOSED = "This account is disabled.";
    public static final String INSUFFICIENT_FUNDS = "Insufficient funds.";
    public static final String TRANSFER_TO_SAME_ACCOUNT = "You cannot transfer to the same account.";

    public BankingAccountAuthorizationException(String message) {
        super(message);
    }

}
