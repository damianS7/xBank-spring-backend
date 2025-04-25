package com.damian.xBank.customer.profile.exception;

public class ProfileNotFoundException extends ProfileException {
    public ProfileNotFoundException(String message) {
        super(message);
    }

    public ProfileNotFoundException() {
        this("Profile not found with: not passed");
    }

    public ProfileNotFoundException(Long id) {
        this("Profile not found with id: " + id);
    }
}
