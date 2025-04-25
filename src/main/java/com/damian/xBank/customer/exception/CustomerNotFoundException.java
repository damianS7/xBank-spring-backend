package com.damian.xBank.customer.exception;

public class CustomerNotFoundException extends CustomerException {

    public CustomerNotFoundException(String email) {
        super("Customer not found with email: " + email);
    }

    public CustomerNotFoundException(Long id) {
        super("Customer not found with id: " + id);
    }
}
