package com.damian.xBank.auth.exception;

public class AuthenticationAccountDisabledException extends AuthenticationException {
    public AuthenticationAccountDisabledException(String message) {
        super("Account is disabled.");
    }
}
