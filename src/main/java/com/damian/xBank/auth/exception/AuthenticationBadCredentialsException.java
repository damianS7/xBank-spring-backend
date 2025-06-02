package com.damian.xBank.auth.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class AuthenticationBadCredentialsException extends BadCredentialsException {
    public static final String BAD_CREDENTIALS = "Bad credentials.";

    public AuthenticationBadCredentialsException(String message) {
        super(message);
    }
}
