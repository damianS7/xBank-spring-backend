package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.transactions.BankingAccountTransactionDTO;
import com.damian.xBank.banking.card.BankingCardDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public record BankingAccountDTO(
        Long id,
        String accountNumber,
        BigDecimal balance,
        BankingAccountType accountType,
        BankingAccountCurrency accountCurrency,
        BankingAccountStatus accountStatus,
        Instant updatedAt,
        BankingCardDTO bankingCard,
        Set<BankingAccountTransactionDTO> bankingAccountTransactions
) {
}
