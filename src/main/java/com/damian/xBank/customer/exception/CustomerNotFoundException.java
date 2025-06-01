package com.damian.xBank.customer.exception;

public class CustomerNotFoundException extends CustomerException {
    public static final String NOT_FOUND = "Customer not found.";

    public CustomerNotFoundException(String message) {
        super(message);
    }
}
