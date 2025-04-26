package com.damian.xBank.banking.card;

public record BankingCardDTO(
        Long id,
        String cardNumber,
        BankingCardType cardType,
        BankingCardStatus cardStatus
) {
}
