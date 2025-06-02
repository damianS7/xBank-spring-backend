package com.damian.xBank.auth.exception;

public class AccountDisabledException extends AuthenticationException {
    public static final String ACCOUNT_DISABLED = "Account is disabled.";

    public AccountDisabledException(String message) {
        super(message);
    }
}
