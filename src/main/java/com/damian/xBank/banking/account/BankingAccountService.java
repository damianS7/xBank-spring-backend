package com.damian.xBank.banking.account;

import net.datafaker.Faker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BankingAccountService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final Faker faker;

    public BankingAccountService(BCryptPasswordEncoder bCryptPasswordEncoder, Faker faker) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.faker = faker;
    }

    public BankingAccount updateBankingAccount(BankingAccountUpdateRequest request) {
        return null;
    }

    public String generateAccountNumber() {
        return faker.finance().iban();
    }
}
