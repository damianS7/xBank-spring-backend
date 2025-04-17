package com.damian.xBank.banking.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankingAccountRepository extends JpaRepository<BankingAccount, Long> {
    Optional<BankingAccount> findByCustomer_Id(Long customerId);
}

