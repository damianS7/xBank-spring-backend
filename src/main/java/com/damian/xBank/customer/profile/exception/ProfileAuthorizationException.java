package com.damian.xBank.customer.profile.exception;

import com.damian.xBank.auth.exception.AuthorizationException;

public class ProfileAuthorizationException extends AuthorizationException {
    public ProfileAuthorizationException(String message) {
        super(message);
    }
}
