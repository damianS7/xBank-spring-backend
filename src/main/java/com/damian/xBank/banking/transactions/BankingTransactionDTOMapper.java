package com.damian.xBank.banking.transactions;

public class BankingTransactionDTOMapper {


    public static BankingTransactionDTO toBankingTransactionDTO(BankingTransaction accountTransaction) {
        return new BankingTransactionDTO(
                accountTransaction.getId(),
                accountTransaction.getAmount(),
                accountTransaction.getTransactionType(),
                accountTransaction.getTransactionStatus(),
                accountTransaction.getDescription(),
                accountTransaction.getCreatedAt(),
                accountTransaction.getUpdatedAt()
        );
    }

}
