package com.damian.xBank.banking.transactions.exception;

import com.damian.xBank.auth.exception.AuthorizationException;

public class BankingTransactionAuthorizationException extends AuthorizationException {
    public static final String TRANSACTION_NOT_BELONG_TO_CUSTOMER = "This transaction does not belong to the customer.";

    public BankingTransactionAuthorizationException(String message) {
        super(message);
    }
}
