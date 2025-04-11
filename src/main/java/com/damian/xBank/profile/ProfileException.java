package com.damian.xBank.profile;

//@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "...")
public class ProfileException extends RuntimeException {
    public ProfileException(String message) {
        super(message);
    }
}
