package com.damian.xBank.banking.account.http.request;

import jakarta.validation.constraints.NotNull;

public record BankingAccountOpenRequest(
        @NotNull(
                message = "Password must not be null"
        )
        String password
) {
}
