package com.damian.xBank.banking.card.http;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record BankingCardSetPinRequest(
        @NotNull(
                message = "PIN must not be null"
        )
        @Pattern(regexp = "\\d{4}", message = "PIN must be exactly 4 digits")
        String pin,

        @NotNull(
                message = "Password must not be null"
        )
        String password
) {
}
