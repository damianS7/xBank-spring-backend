package com.damian.xBank.banking.card.exception;

public class BankingCardNotFoundException extends BankingCardException {
    public BankingCardNotFoundException(String message) {
        super(message);
    }

    public BankingCardNotFoundException() {
        this("Banking card not found with id: not specified.");
    }

    public BankingCardNotFoundException(Long id) {
        this("Banking card not found with id: " + id);
    }
}
