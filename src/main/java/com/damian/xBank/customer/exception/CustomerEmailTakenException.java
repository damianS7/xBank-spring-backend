package com.damian.xBank.customer.exception;

public class CustomerEmailTakenException extends CustomerException {
    public static final String EMAIL_TAKEN = "Email is already taken.";

    public CustomerEmailTakenException(String message) {
        super(message);
    }
}
