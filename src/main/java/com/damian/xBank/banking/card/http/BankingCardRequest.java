package com.damian.xBank.banking.card.http;

import com.damian.xBank.banking.card.BankingCardType;
import jakarta.validation.constraints.NotNull;

public record BankingCardRequest(
        @NotNull(
                message = "Card type must not be null"
        ) BankingCardType cardType
) {
}
