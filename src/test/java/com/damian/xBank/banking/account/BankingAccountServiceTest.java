package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.http.request.BankingAccountOpenRequest;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import net.datafaker.Faker;
import net.datafaker.providers.base.Finance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Habilita Mockito en JUnit 5
@ExtendWith(MockitoExtension.class)
public class BankingAccountServiceTest {

    @Mock
    private BankingAccountRepository bankingAccountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private Faker faker;

    @Mock
    private Finance finance;

    @InjectMocks
    private BankingAccountService bankingAccountService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer(99L, "customer@test.com", "3hri2rhid;/!");

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                customer, null, Collections.emptyList()));

        when(faker.finance()).thenReturn(finance);
        when(finance.iban()).thenReturn("ES1234567890123456789012");
    }

    @Test
    void shouldCreateBankingAccount() {
        // given
        BankingAccountOpenRequest request = new BankingAccountOpenRequest(
                BankingAccountType.SAVINGS,
                BankingAccountCurrency.EUR
        );

        BankingAccount savedAccount = new BankingAccount();
        savedAccount.setAccountCurrency(request.accountCurrency());
        savedAccount.setAccountType(request.accountType());
        savedAccount.setAccountNumber(faker.finance().iban());

        // when
        when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(bankingAccountRepository.save(any(BankingAccount.class))).thenReturn(savedAccount);

        bankingAccountService.openBankingAccount(request);

        // then
        assertThat(savedAccount.getAccountCurrency()).isEqualTo(request.accountCurrency());
        assertThat(savedAccount.getAccountType()).isEqualTo(request.accountType());
        assertThat(savedAccount.getAccountNumber()).isNotBlank();
        assertThat(savedAccount.getBalance()).isEqualTo(BigDecimal.valueOf(0));
        verify(bankingAccountRepository, times(1)).save(any(BankingAccount.class));
    }
}
