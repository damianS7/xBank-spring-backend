package com.damian.xBank.common.exception;

public class Exceptions {
    public static class AUTH {
        public static final String BAD_CREDENTIALS = "Bad credentials.";
    }


    public static class CUSTOMER {
        public static final String DISABLED = "Customer is disabled.";
        public static final String EMAIL_TAKEN = "Email is already taken.";
        public static final String NOT_FOUND = "Customer not found.";
    }

    public static class CARD {
        public static final String NOT_FOUND = "Card not found.";
        public static final String LOCKED = "Card is locked.";
        public static final String INVALID_PIN = "Incorrect card pin.";
        public static final String DISABLED = "Card is disabled.";
        public static final String ACCESS_FORBIDDEN = "You are not the owner of this card.";
        public static final String INSUFFICIENT_FUNDS = "Insufficient funds.";
    }

    public static class TRANSACTION {
        public static final String NOT_FOUND = "Transaction not found.";
        public static final String ACCESS_FORBIDDEN = "You are not the owner of this transaction.";
        public static final String INVALID_TYPE = "Invalid transaction type";
    }

    public static class ACCOUNT {
        public static final String SUSPENDED = "This account is suspended.";
        public static final String CLOSED = "This account is disabled.";
        public static final String SAME_DESTINATION = "You cannot transfer to the same account.";
        public static final String NOT_FOUND = "Account not found.";
        public static final String CARD_LIMIT = "The account has reached the maximum number of cards allowed.";
        public static final String LOCKED = "Account is locked.";
        public static final String DISABLED = "Account is disabled.";
        public static final String ACCESS_FORBIDDEN = "You are not the owner of this account.";
        public static final String INSUFFICIENT_FUNDS = "Insufficient funds.";
    }

    public static class PROFILE {
        public static final String NOT_FOUND = "Profile not found.";
        public static final String INVALID_FIELD = "Field is invalid.";
        public static final String ACCESS_FORBIDDEN = "You are not authorized to access this profile.";

        public static class IMAGE {
            public static final String NOT_FOUND = "Profile photo not found.";
            public static final String FILE_SIZE_LIMIT = "Profile photo is too large.";
            public static final String ONLY_IMAGES_ALLOWED = "Profile photo must be an image.";
            public static final String EMPTY_FILE = "File is empty.";
            public static final String UPLOAD_FAILED = "Profile photo upload failed.";
        }
    }
}
