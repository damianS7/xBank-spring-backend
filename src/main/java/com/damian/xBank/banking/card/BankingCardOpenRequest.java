package com.damian.xBank.banking.card;

import jakarta.validation.constraints.NotNull;

public record BankingCardOpenRequest(
        @NotNull(
                message = "Card type must not be null"
        ) BankingCardType cardType
) {
}
