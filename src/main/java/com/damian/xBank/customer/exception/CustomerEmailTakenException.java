package com.damian.xBank.customer.exception;

public class CustomerEmailTakenException extends CustomerException {
    public CustomerEmailTakenException(String email) {
        super("Email " + email + " is already taken.");
    }
}
