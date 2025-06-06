package com.damian.xBank.banking.card;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface BankingCardRepository extends JpaRepository<BankingCard, Long> {
    Set<BankingCard> findByBankingAccountId(Long bankingAccountId);

    @Query("SELECT cards FROM BankingCard cards WHERE cards.bankingAccount.customer.id = :customerId")
    Set<BankingCard> findCardsByCustomerId(@Param("customerId") Long customerId);
}


