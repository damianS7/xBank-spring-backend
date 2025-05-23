package com.damian.xBank.banking.account.http.request;

import jakarta.validation.constraints.NotNull;

public record BankingAccountAliasUpdateRequest(
        @NotNull(message = "Alias must not be null")
        String alias
) {
}
