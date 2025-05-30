package com.damian.xBank.banking.card.exception;

public class BankingCardMaximumCardsPerAccountLimitReached extends BankingCardException {
    public BankingCardMaximumCardsPerAccountLimitReached(String message) {
        super(message);
    }

    public BankingCardMaximumCardsPerAccountLimitReached(int maxCardsPerAccount) {
        this("The account has reached the maximum number of cards allowed.");
    }

    public BankingCardMaximumCardsPerAccountLimitReached() {
        this("The account has reached the maximum number of cards allowed.");
    }
}
// BankingCardMaximumCardsPerAccountLimitReached