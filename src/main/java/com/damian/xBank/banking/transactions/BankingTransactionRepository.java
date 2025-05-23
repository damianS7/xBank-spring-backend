package com.damian.xBank.banking.transactions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface BankingTransactionRepository extends JpaRepository<BankingTransaction, Long> {
    Set<BankingTransaction> findByBankingCardId(Long bankingCardId);
}

