package com.damian.xBank.banking.transactions;

import org.springframework.data.domain.Page;

import java.util.Set;
import java.util.stream.Collectors;

public class BankingTransactionDTOMapper {


    public static BankingTransactionDTO toBankingTransactionDTO(BankingTransaction accountTransaction) {
        return new BankingTransactionDTO(
                accountTransaction.getId(),
                accountTransaction.getAssociatedBankingAccount().getId(),
                accountTransaction.getBankingCard() != null ? accountTransaction.getBankingCard().getId() : null,
                accountTransaction.getAmount(),
                accountTransaction.getAssociatedBankingAccount().getBalance(),
                accountTransaction.getTransactionType(),
                accountTransaction.getTransactionStatus(),
                accountTransaction.getDescription(),
                accountTransaction.getCreatedAt(),
                accountTransaction.getUpdatedAt()
        );
    }

    public static Set<BankingTransactionDTO> toBankingTransactionSetDTO(Set<BankingTransaction> accountTransactions) {
        return accountTransactions.stream().map(
                BankingTransactionDTOMapper::toBankingTransactionDTO
        ).collect(Collectors.toSet());
    }

    public static Page<BankingTransactionDTO> toBankingTransactionPageDTO(Page<BankingTransaction> accountTransactions) {
        return accountTransactions.map(
                BankingTransactionDTOMapper::toBankingTransactionDTO
        );
    }

}
