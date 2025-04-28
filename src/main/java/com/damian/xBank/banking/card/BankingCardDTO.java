package com.damian.xBank.banking.card;

import java.time.Instant;

public record BankingCardDTO(
        Long id,
        String cardNumber,
        BankingCardType cardType,
        BankingCardStatus cardStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
