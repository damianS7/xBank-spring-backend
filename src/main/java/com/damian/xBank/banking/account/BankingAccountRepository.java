package com.damian.xBank.banking.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankingAccountRepository extends JpaRepository<BankingAccount, Long> {
    List<BankingAccount> findByCustomer_Id(Long customerId);
}

