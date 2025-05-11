package com.damian.xBank.banking.account;

import com.damian.xBank.banking.card.BankingCardDTO;
import com.damian.xBank.banking.card.BankingCardDTOMapper;
import com.damian.xBank.banking.transactions.BankingTransactionDTO;
import com.damian.xBank.banking.transactions.BankingTransactionDTOMapper;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BankingAccountDTOMapper {
    public static BankingAccountDTO toBankingAccountDTO(BankingAccount bankingAccount) {
        Set<BankingCardDTO> bankingCardsDTO = Optional
                .ofNullable(bankingAccount.getBankingCards())
                .orElseGet(Collections::emptySet)
                .stream()
                .map(BankingCardDTOMapper::toBankingCardDTO)
                .collect(Collectors.toSet());

        Set<BankingTransactionDTO> bankingTransactionsDTO = Optional
                .ofNullable(bankingAccount.getAccountTransactions())
                .orElseGet(Collections::emptySet)
                .stream()
                .map(BankingTransactionDTOMapper::toBankingTransactionDTO)
                .collect(Collectors.toSet());

        return new BankingAccountDTO(
                bankingAccount.getId(),
                bankingAccount.getAccountNumber(),
                bankingAccount.getBalance(),
                bankingAccount.getAccountType(),
                bankingAccount.getAccountCurrency(),
                bankingAccount.getAccountStatus(),
                bankingCardsDTO,
                bankingTransactionsDTO,
                bankingAccount.getCreatedAt(),
                bankingAccount.getUpdatedAt()
        );
    }

    public static Set<BankingAccountDTO> toBankingAccountSetDTO(Set<BankingAccount> bankingAccounts) {
        return bankingAccounts.stream().map(
                BankingAccountDTOMapper::toBankingAccountDTO
        ).collect(Collectors.toSet());
    }


}
