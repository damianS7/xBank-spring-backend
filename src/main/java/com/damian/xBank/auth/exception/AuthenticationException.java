package com.damian.xBank.auth.exception;

import com.damian.xBank.common.exception.ApplicationException;

public class AuthenticationException extends ApplicationException {
    public AuthenticationException(String message) {
        super(message);
    }
}
