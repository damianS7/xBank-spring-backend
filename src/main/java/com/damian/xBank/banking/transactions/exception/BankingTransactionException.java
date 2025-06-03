package com.damian.xBank.banking.transactions.exception;

import com.damian.xBank.common.exception.ApplicationException;

public class BankingTransactionException extends ApplicationException {
    public BankingTransactionException(String message) {
        super(message);
    }
}
