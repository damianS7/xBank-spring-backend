package com.damian.xBank.banking.card.http;

import com.damian.xBank.banking.card.BankingCardLockStatus;
import jakarta.validation.constraints.NotNull;

public record BankingCardLockStatusRequest(
        @NotNull(
                message = "Lock status must not be null"
        )
        BankingCardLockStatus lockStatus
) {
}
