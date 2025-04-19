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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        customer = new Customer(99L, "customer@test.com", "123456");

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                customer, null, Collections.emptyList()));
    }

    @Test
    void shouldCreateBankingAccount() {
        // given
        final String accountNumber = "US99 0000 1111 1122 3333 4444";
        BankingAccountOpenRequest request = new BankingAccountOpenRequest(
                BankingAccountType.SAVINGS,
                BankingAccountCurrency.EUR
        );

        BankingAccount bankingAccount = new BankingAccount(customer);
        bankingAccount.setAccountCurrency(request.accountCurrency());
        bankingAccount.setAccountType(request.accountType());
        bankingAccount.setAccountNumber(accountNumber);

        // when
        when(faker.finance()).thenReturn(finance);
        when(finance.iban()).thenReturn("US99 0000 1111 1122 3333 4444");
        when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.of(customer));
        when(bankingAccountRepository.save(any(BankingAccount.class))).thenReturn(bankingAccount);

        BankingAccount savedAccount = bankingAccountService.openBankingAccount(request);

        // then
        assertThat(savedAccount.getAccountCurrency()).isEqualTo(request.accountCurrency());
        assertThat(savedAccount.getAccountType()).isEqualTo(request.accountType());
        assertThat(savedAccount.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(savedAccount.getBalance()).isEqualTo(BigDecimal.valueOf(0));
        verify(bankingAccountRepository, times(1)).save(any(BankingAccount.class));
    }

    @Test
    void shouldGetBankingAccountsFromCustomer() {
        // given
        List<BankingAccount> bankingAccounts = new ArrayList<>();
        BankingAccount bankingAccountA = new BankingAccount(customer);
        bankingAccountA.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountA.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountA.setAccountNumber("US99 0000 1111 1122 3333 4444");
        bankingAccounts.add(bankingAccountA);

        BankingAccount bankingAccountB = new BankingAccount(customer);
        bankingAccountB.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountB.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountB.setAccountNumber("US99 0000 1111 1122 3333 6666");
        bankingAccounts.add(bankingAccountB);

        // when
        when(bankingAccountRepository.findByCustomer_Id(anyLong())).thenReturn(bankingAccounts);

        Set<BankingAccountDTO> result = bankingAccountService.getBankingAccounts(customer.getId());

        // then
        assertThat(result.size()).isEqualTo(2);
        verify(bankingAccountRepository, times(1)).findByCustomer_Id(anyLong());
    }
}
