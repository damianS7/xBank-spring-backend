package com.damian.xBank.banking.card.exception;

import com.damian.xBank.auth.exception.AuthorizationException;

public class BankingCardAuthorizationException extends AuthorizationException {
    public static final String CARD_DOES_NOT_BELONG_TO_CUSTOMER = "This card does not belong to the customer.";
    public static final String INVALID_PIN = "Incorrect card pin.";
    public static final String CARD_DISABLED = "Card is disabled.";
    public static final String CARD_LOCKED = "Card is locked.";
    public static final String INSUFFICIENT_FUNDS = "Insufficient funds.";

    public BankingCardAuthorizationException(String message) {
        super(message);
    }
}
