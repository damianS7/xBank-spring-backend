package com.damian.xBank.banking.card.exception;

public class BankingCardMaximumCardsPerAccountLimitReached extends BankingCardException {
    public BankingCardMaximumCardsPerAccountLimitReached(String message) {
        super(message);
    }
}