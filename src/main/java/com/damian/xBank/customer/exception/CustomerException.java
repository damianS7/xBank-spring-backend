package com.damian.xBank.customer.exception;

import com.damian.xBank.common.exception.ApplicationException;

public class CustomerException extends ApplicationException {
    public CustomerException(String message) {
        super(message);
    }
}
