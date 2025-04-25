package com.damian.xBank.common.exception;

public class PasswordMismatchException extends ApplicationException {
    public PasswordMismatchException(String message) {
        super(message);
    }

    public PasswordMismatchException() {
        this("Password does not match.");
    }
}
