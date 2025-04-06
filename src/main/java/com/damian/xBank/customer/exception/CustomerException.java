package com.damian.xBank.customer.exception;

//@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "...")
public class CustomerException extends RuntimeException {
    public CustomerException(String message) {
        super(message);
    }
}
