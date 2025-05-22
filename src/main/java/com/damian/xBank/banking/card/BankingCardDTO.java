package com.damian.xBank.banking.card;

import java.time.Instant;
import java.time.LocalDate;

public record BankingCardDTO(
        Long id,
        Long bankingAccountId,
        String cardNumber,
        String cardCVV,
        String cardPIN,
        LocalDate expiredDate,
        BankingCardType cardType,
        BankingCardStatus cardStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
