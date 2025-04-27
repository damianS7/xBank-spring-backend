package com.damian.xBank.banking.account;

import com.damian.xBank.banking.card.BankingCardDTO;
import com.damian.xBank.banking.transactions.BankingTransactionDTO;

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
        Set<BankingCardDTO> bankingCards,
        Set<BankingTransactionDTO> bankingAccountTransactions
) {
}
