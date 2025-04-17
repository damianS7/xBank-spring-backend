package com.damian.xBank.banking.account;

import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import net.datafaker.Faker;
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

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        accountRepository.deleteAll();
    }

    @Test
    void shouldSaveBankingAccount() {
        // given
        Customer customer = new Customer("demo@test.com", "1234");
        customerRepository.save(customer);

        BankingAccount account = new BankingAccount();
        account.setCustomer(customer);
        account.setAccountNumber("AA11 0000 0000 0000 0000");
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
        assertThat(result.get().getCustomer().getId()).isEqualTo(account.getCustomer().getId());
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
        Customer customer = new Customer("demo@test.com", "1234");
        customerRepository.save(customer);

        BankingAccount account = new BankingAccount();
        account.setCustomer(customer);
        account.setAccountNumber("AA11 0000 0000 0000 0000");
        account.setAccountCurrency(BankingAccountCurrency.EUR);
        account.setAccountStatus(BankingAccountStatus.OPEN);
        account.setAccountType(BankingAccountType.SAVINGS);
        account.setBalance(BigDecimal.valueOf(200));
        accountRepository.save(account);

        // when
        account.setBalance(BigDecimal.valueOf(500));
        accountRepository.save(account);

        // then
        Optional<BankingAccount> result = accountRepository.findById(account.getId());
        assertThat(result.isPresent());
        assertThat(result.get().getBalance()).isEqualTo(BigDecimal.valueOf(500));
    }
}
