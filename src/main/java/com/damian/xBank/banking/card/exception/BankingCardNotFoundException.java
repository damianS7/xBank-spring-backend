package com.damian.xBank.banking.card.exception;

public class BankingCardNotFoundException extends BankingCardException {
    public static final String CARD_NOT_FOUND = "Card not found.";

    public BankingCardNotFoundException(String message) {
        super(message);
    }
}
