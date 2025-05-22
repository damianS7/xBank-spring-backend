package com.damian.xBank.banking.card;

public enum BankingCardStatus {
    ENABLED,
    DISABLED, // disabled by default
    SUSPENDED, // suspended by admin
    LOCKED, // temporary locked by user
    BLOCKED // blocked by admin
}
