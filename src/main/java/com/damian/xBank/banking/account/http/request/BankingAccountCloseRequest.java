package com.damian.xBank.banking.account.http.request;

import jakarta.validation.constraints.NotNull;

public record BankingAccountCloseRequest(
        @NotNull(
                message = "Password must not be null"
        )
        String password
) {
}
