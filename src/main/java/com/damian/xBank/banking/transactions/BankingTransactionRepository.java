package com.damian.xBank.banking.transactions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankingTransactionRepository extends JpaRepository<BankingTransaction, Long> {
}

