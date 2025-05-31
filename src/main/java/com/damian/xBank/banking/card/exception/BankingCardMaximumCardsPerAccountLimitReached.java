package com.damian.xBank.banking.card.exception;

public class BankingCardMaximumCardsPerAccountLimitReached extends BankingCardException {
    public static final String LIMIT_REACHED = "The account has reached the maximum number of cards allowed.";

    public BankingCardMaximumCardsPerAccountLimitReached(String message) {
        super(message);
    }
}