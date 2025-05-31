package com.damian.xBank.banking.transactions.exception;

import com.damian.xBank.common.exception.ApplicationException;

public class BankingTransactionException extends ApplicationException {
    public static final String INVALID_TRANSACTION_TYPE = "Invalid transaction type";

    public BankingTransactionException(String message) {
        super(message);
    }
}
