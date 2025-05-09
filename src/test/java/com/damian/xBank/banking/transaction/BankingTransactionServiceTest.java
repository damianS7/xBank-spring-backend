package com.damian.xBank.banking.transaction;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountRepository;
import com.damian.xBank.banking.transactions.*;
import com.damian.xBank.banking.transactions.exception.BankingTransactionAuthorizationException;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import net.datafaker.Faker;
import net.datafaker.providers.base.Finance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankingTransactionServiceTest {

    @Mock
    private BankingAccountRepository bankingAccountRepository;

    @Mock
    private BankingTransactionRepository bankingTransactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private Faker faker;

    @Mock
    private Finance finance;

    @InjectMocks
    private BankingTransactionService bankingTransactionService;

    private Customer customerA;
    private Customer customerB;
    private Customer customerAdmin;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        customerA = new Customer(99L, "customerA@test.com", "123456");
        customerB = new Customer(92L, "customerB@test.com", "123456");
        customerAdmin = new Customer(95L, "admin@test.com", "123456");
        customerAdmin.setRole(CustomerRole.ADMIN);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    void setUpContext(Customer customer) {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(customer);
    }

    @Test
    @DisplayName("It should update transaction status.")
    void shouldUpdateTransactionStatus() {
        // given
        setUpContext(customerAdmin);

        BankingTransactionPatchRequest request = new BankingTransactionPatchRequest(
                BankingTransactionStatus.COMPLETED
        );

        final String accountNumber = "US99 0000 1111 1122 3333 4444";

        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountNumber(accountNumber);

        BankingTransaction bankingTransaction = new BankingTransaction();
        bankingTransaction.setAmount(BigDecimal.valueOf(100));
        bankingTransaction.setTransactionType(BankingTransactionType.CARD_CHARGE);
        bankingTransaction.setDescription("Amazon.com");
        bankingTransaction.setTransactionStatus(BankingTransactionStatus.PENDING);

        bankingTransaction.setAssociatedBankingAccount(bankingAccount);
        bankingAccount.addAccountTransaction(bankingTransaction);

        bankingAccountRepository.save(bankingAccount);

        // when
        when(bankingTransactionRepository.findById(bankingTransaction.getId())).thenReturn(Optional.of(
                bankingTransaction));
        when(bankingTransactionRepository.save(any(BankingTransaction.class))).thenReturn(bankingTransaction);

        BankingTransaction savedTransaction =
                bankingTransactionService.patchStatusTransaction(bankingTransaction.getId(), request);

        // then
        assertThat(savedTransaction.getTransactionStatus()).isEqualTo(request.transactionStatus());
        verify(bankingTransactionRepository, times(1)).save(any(BankingTransaction.class));
    }

    @Test
    @DisplayName("It should not update transaction status when you are not admin.")
    void shouldNotUpdateTransactionStatusWhenYouAreNotAdmin() {
        // given
        setUpContext(customerA);

        BankingTransactionPatchRequest request = new BankingTransactionPatchRequest(
                BankingTransactionStatus.COMPLETED
        );

        final String accountNumber = "US99 0000 1111 1122 3333 4444";

        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountNumber(accountNumber);

        BankingTransaction bankingTransaction = new BankingTransaction();
        bankingTransaction.setAmount(BigDecimal.valueOf(100));
        bankingTransaction.setTransactionType(BankingTransactionType.CARD_CHARGE);
        bankingTransaction.setDescription("Amazon.com");
        bankingTransaction.setTransactionStatus(BankingTransactionStatus.PENDING);

        bankingTransaction.setAssociatedBankingAccount(bankingAccount);
        bankingAccount.addAccountTransaction(bankingTransaction);

        bankingAccountRepository.save(bankingAccount);

        // when
        BankingTransactionAuthorizationException exception = assertThrows(
                BankingTransactionAuthorizationException.class,
                () -> bankingTransactionService.patchStatusTransaction(bankingTransaction.getId(), request)
        );

        // then
        assertTrue(exception.getMessage().contains("Unauthorized access to transaction."));
    }
}
