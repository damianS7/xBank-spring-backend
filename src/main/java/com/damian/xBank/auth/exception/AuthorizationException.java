package com.damian.xBank.auth.exception;

import com.damian.xBank.common.exception.ApplicationException;

public class AuthorizationException extends ApplicationException {
    public AuthorizationException(String message) {
        super("Unauthorized access: " + message);
    }
}
