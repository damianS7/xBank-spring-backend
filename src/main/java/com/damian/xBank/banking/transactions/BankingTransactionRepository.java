package com.damian.xBank.banking.transactions;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankingTransactionRepository extends JpaRepository<BankingTransaction, Long> {
    //    Set<BankingTransaction> findByBankingCardId(Long bankingCardId);
    Page<BankingTransaction> findByBankingCardId(Long bankingCardId, Pageable pageable);

    Page<BankingTransaction> findByBankingAccountId(Long bankingAccountId, Pageable pageable);
}

