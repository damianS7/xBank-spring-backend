package com.damian.xBank.banking.card;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface BankingCardRepository extends JpaRepository<BankingCard, Long> {
    Set<BankingCard> findByBankingAccountId(Long bankingAccountId);
}

