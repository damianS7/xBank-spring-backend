package com.damian.xBank.banking.card.exception;

public class BankingCardLimitCardsPerAccountException extends BankingCardException {
    public BankingCardLimitCardsPerAccountException(String message) {
        super(message);
    }

    public BankingCardLimitCardsPerAccountException(int maxCardsPerAccount) {
        this("The account already has reached the maximum number of cards allowed.");
    }

    public BankingCardLimitCardsPerAccountException() {
        this("The account already has reached the maximum number of cards allowed.");
    }
}
