package com.damian.xBank.banking.card.http;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BankingCardSetDailyLimitRequest(
        @NotNull(
                message = "Daily limit must not be null"
        )
        @Positive
        BigDecimal dailyLimit
) {
}
