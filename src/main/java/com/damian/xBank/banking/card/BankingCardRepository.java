package com.damian.xBank.banking.card;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankingCardRepository extends JpaRepository<BankingCard, Long> {
    BankingCard findByBankingAccountId(Long bankingAccountId);
}

