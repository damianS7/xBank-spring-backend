package com.damian.xBank.banking.account;

import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class BankingAccountRepositoryTest {

    @Autowired
    private BankingAccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
    }

    @Test
    void shouldSaveBankingAccount() {
        // given
        final String customerEmail = "customer@test.com";
        final String customerPassword = "123456";
        final String accountNumber = "US00 1111 2222 3333 4444 5555";

        Customer customer = new Customer(customerEmail, customerPassword);
        customerRepository.save(customer);

        BankingAccount account = new BankingAccount();
        account.setOwner(customer);
        account.setAccountNumber(accountNumber);
        account.setAccountCurrency(BankingAccountCurrency.EUR);
        account.setAccountStatus(BankingAccountStatus.OPEN);
        account.setAccountType(BankingAccountType.SAVINGS);
        account.setBalance(BigDecimal.valueOf(200));

        // when
        accountRepository.save(account);

        // then
        Optional<BankingAccount> result = accountRepository.findById(account.getId());
        assertThat(result.isPresent());
        assertThat(result.get().getId()).isEqualTo(account.getId());
        assertThat(result.get().getAccountNumber()).isEqualTo(account.getAccountNumber());
        assertThat(result.get().getOwner().getId()).isEqualTo(account.getOwner().getId());
    }

    @Test
    void shouldNotFindBankingAccount() {
        // given
        Long profileId = -1L;

        // when
        boolean exists = accountRepository.existsById(-1L);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldUpdateBankingAccount() {
        // given
        final String customerEmail = "customer@test.com";
        final String customerPassword = "123456";
        final String accountNumber = "US00 1111 2222 3333 4444 5555";

        Customer customer = new Customer(customerEmail, customerPassword);
        customerRepository.save(customer);

        BankingAccount account = new BankingAccount();
        account.setOwner(customer);
        account.setAccountNumber(accountNumber);
        account.setAccountCurrency(BankingAccountCurrency.EUR);
        account.setAccountStatus(BankingAccountStatus.OPEN);
        account.setAccountType(BankingAccountType.SAVINGS);
        account.setBalance(BigDecimal.valueOf(200));
        accountRepository.save(account);

        // when
        final BigDecimal updatedBalance = BigDecimal.valueOf(600);
        account.setBalance(updatedBalance);
        accountRepository.save(account);

        // then
        Optional<BankingAccount> result = accountRepository.findById(account.getId());
        assertThat(result.isPresent());
        assertThat(result.get().getBalance()).isEqualTo(updatedBalance);
    }
}
