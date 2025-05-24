package com.damian.xBank.banking.card;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BankingCardDTO(
        Long id,
        Long bankingAccountId,
        String cardNumber,
        String cardCVV,
        String cardPIN,
        BigDecimal dailyLimit,
        LocalDate expiredDate,
        BankingCardType cardType,
        BankingCardStatus cardStatus,
        BankingCardLockStatus lockStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
