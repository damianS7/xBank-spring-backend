package com.damian.xBank.banking.transaction;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountRepository;
import com.damian.xBank.banking.transactions.*;
import com.damian.xBank.banking.transactions.exception.BankingTransactionAuthorizationException;
import com.damian.xBank.banking.transactions.http.BankingTransactionUpdateStatusRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
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
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(customer);
    }

    @Test
    @DisplayName("It should create transaction.")
    void shouldCreateTransaction() {
        // given
        BankingAccount givenBankingAccount = new BankingAccount(customerA);
        givenBankingAccount.setAccountNumber("US9900001111112233334444");
        String givenDescription = "Account deposit";
        BigDecimal givenAmount = BigDecimal.valueOf(100);
        BankingTransactionType givenTransactionType = BankingTransactionType.DEPOSIT;

        // when
        BankingTransaction createdBankingTransaction = bankingTransactionService.createTransaction(
                givenBankingAccount,
                givenTransactionType,
                givenAmount,
                givenDescription
        );

        // then
        assertThat(createdBankingTransaction).isNotNull();
        assertThat(createdBankingTransaction.getAmount()).isEqualTo(givenAmount);
        assertThat(createdBankingTransaction.getTransactionType()).isEqualTo(givenTransactionType);
        assertThat(createdBankingTransaction.getDescription()).isEqualTo(givenDescription);
    }

    @Test
    @DisplayName("It should persist transaction.")
    void shouldPersistTransaction() {
        // given
        BankingAccount givenBankingAccount = new BankingAccount(customerA);
        givenBankingAccount.setAccountNumber("US9900001111112233334444");

        BankingTransaction givenBankingTransaction = new BankingTransaction(givenBankingAccount);
        givenBankingTransaction.setAmount(BigDecimal.valueOf(100));
        givenBankingTransaction.setTransactionType(BankingTransactionType.DEPOSIT);
        givenBankingTransaction.setDescription("Account deposit");

        // when
        when(bankingTransactionRepository.save(any(BankingTransaction.class)))
                .thenReturn(givenBankingTransaction);
        BankingTransaction createdBankingTransaction = bankingTransactionService.persistTransaction(
                givenBankingTransaction
        );

        // then
        assertThat(createdBankingTransaction).isNotNull();
        assertThat(createdBankingTransaction.getAmount()).isEqualTo(givenBankingTransaction.getAmount());
        assertThat(createdBankingTransaction.getTransactionType()).isEqualTo(givenBankingTransaction.getTransactionType());
        assertThat(createdBankingTransaction.getDescription()).isEqualTo(givenBankingTransaction.getDescription());
        assertThat(givenBankingAccount.getAccountTransactions().size()).isEqualTo(1);
        verify(bankingTransactionRepository, times(1)).save(any(BankingTransaction.class));
    }

    @Test
    @DisplayName("It should update transaction status.")
    void shouldUpdateTransactionStatus() {
        // given
        setUpContext(customerAdmin);

        BankingTransactionUpdateStatusRequest request = new BankingTransactionUpdateStatusRequest(
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
                bankingTransactionService.updateTransactionStatus(bankingTransaction.getId(), request);

        // then
        assertThat(savedTransaction.getTransactionStatus()).isEqualTo(request.transactionStatus());
        verify(bankingTransactionRepository, times(1)).save(any(BankingTransaction.class));
    }

    @Test
    @DisplayName("It should not update transaction status when you are not admin.")
    void shouldNotUpdateTransactionStatusWhenYouAreNotAdmin() {
        // given
        setUpContext(customerA);

        BankingTransactionUpdateStatusRequest request = new BankingTransactionUpdateStatusRequest(
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
                () -> bankingTransactionService.updateTransactionStatus(bankingTransaction.getId(), request)
        );

        // then
        assertTrue(exception.getMessage().contains(
                BankingTransactionAuthorizationException.TRANSACTION_NOT_BELONG_TO_CUSTOMER
        ));
    }
}
