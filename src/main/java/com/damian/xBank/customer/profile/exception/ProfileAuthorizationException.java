package com.damian.xBank.customer.profile.exception;

import com.damian.xBank.auth.exception.AuthorizationException;

public class ProfileAuthorizationException extends AuthorizationException {
    public static final String PROFILE_NOT_BELONG_TO_CUSTOMER = "This profile does not belong to the customer.";

    public ProfileAuthorizationException(String message) {
        super(message);
    }
}
