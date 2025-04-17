package com.damian.xBank.banking.account.http.request;

import com.damian.xBank.banking.account.BankingAccountCurrency;
import com.damian.xBank.banking.account.BankingAccountType;

public record BankingAccountOpenRequest(
        BankingAccountType accountType,
        BankingAccountCurrency accountCurrency
) {
}
