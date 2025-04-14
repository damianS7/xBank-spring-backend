package com.damian.xBank.banking.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankingAccountRepository extends JpaRepository<BankingAccount, Long> {
}

