package com.damian.xBank.customer.profile.exception;

import com.damian.xBank.common.exception.ApplicationException;

public class ProfileException extends ApplicationException {
    public static final String NOT_FOUND = "Profile not found.";
    public static final String INVALID_FIELD = "Field is invalid.";

    public static class AUTHORIZATION {
        public static final String ACCESS_FORBIDDEN = "You are not authorized to access this profile.";
    }

    public static class PROFILE_IMAGE {
        public static final String NOT_FOUND = "Profile photo not found.";
        public static final String FILE_SIZE_LIMIT = "Profile photo is too large.";
        public static final String ONLY_IMAGES_ALLOWED = "Profile photo must be an image.";
        public static final String EMPTY_FILE = "File is empty.";
        public static final String UPLOAD_FAILED = "Profile photo upload failed.";
    }

    public ProfileException(String message) {
        super(message);
    }
}
