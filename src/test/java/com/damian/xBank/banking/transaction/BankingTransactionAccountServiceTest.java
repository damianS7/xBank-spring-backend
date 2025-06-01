package com.damian.xBank.banking.transaction;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountRepository;
import com.damian.xBank.banking.account.BankingAccountStatus;
import com.damian.xBank.banking.account.exception.BankingAccountAuthorizationException;
import com.damian.xBank.banking.account.exception.BankingAccountNotFoundException;
import com.damian.xBank.banking.transactions.*;
import com.damian.xBank.banking.transactions.http.BankingAccountTransactionRequest;
import com.damian.xBank.common.exception.PasswordMismatchException;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BankingTransactionAccountServiceTest {

    @Mock
    private BankingAccountRepository bankingAccountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private Faker faker;

    @Mock
    private Finance finance;

    @Mock
    private BankingTransactionService bankingTransactionService;

    @InjectMocks
    private BankingTransactionAccountService bankingTransactionAccountService;

    private Customer customerA;
    private Customer customerB;
    private Customer customerAdmin;

    private final String RAW_PASSWORD = "123456";

    @BeforeEach
    void setUp() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        customerRepository.deleteAll();
        customerA = new Customer(99L, "customerA@test.com", bCryptPasswordEncoder.encode(RAW_PASSWORD));
        customerB = new Customer(92L, "customerB@test.com", bCryptPasswordEncoder.encode(RAW_PASSWORD));
        customerAdmin = new Customer(95L, "admin@test.com", bCryptPasswordEncoder.encode(RAW_PASSWORD));
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
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(customer);
    }

    @Test
    @DisplayName("Should process transaction and deposit")
    void shouldProcessTransactionRequestAndDeposit() {
        // given
        setUpContext(customerA);

        BigDecimal givenDepositAmount = BigDecimal.valueOf(3000);

        BankingAccount givenBankingAccount = new BankingAccount(customerA);
        givenBankingAccount.setId(2L);
        givenBankingAccount.setBalance(BigDecimal.ZERO);
        givenBankingAccount.setAccountNumber("US9900001111112233334444");

        BankingTransaction givenBankingTransaction = new BankingTransaction(givenBankingAccount);
        givenBankingTransaction.setTransactionType(BankingTransactionType.DEPOSIT);
        givenBankingTransaction.setAmount(givenDepositAmount);

        BankingAccountTransactionRequest givenRequest = new BankingAccountTransactionRequest(
                givenBankingAccount.getAccountNumber(),
                BankingTransactionType.DEPOSIT,
                "DEPOSIT",
                givenDepositAmount,
                RAW_PASSWORD
        );

        when(bankingAccountRepository.findById(givenBankingAccount.getId())).thenReturn(Optional.of(
                givenBankingAccount));

        when(bankingTransactionService.createTransaction(
                any(BankingAccount.class),
                any(BankingTransactionType.class),
                any(BigDecimal.class),
                any(String.class)
        )).thenReturn(givenBankingTransaction);

        when(bankingTransactionService.persistTransaction(
                any(BankingTransaction.class)
        )).thenReturn(givenBankingTransaction);

        // then
        BankingTransaction transaction = bankingTransactionAccountService.processTransactionRequest(
                givenBankingAccount.getId(),
                givenRequest
        );

        // then
        assertThat(transaction).isNotNull();
        assertThat(transaction.getTransactionType()).isEqualTo(givenBankingTransaction.getTransactionType());
        assertThat(transaction.getTransactionStatus()).isEqualTo(BankingTransactionStatus.COMPLETED);
        assertThat(givenBankingAccount.getBalance()).isEqualTo(givenDepositAmount);
    }

    @Test
    @DisplayName("Should process transaction and transfer to")
    void shouldProcessTransactionRequestAndTransferTo() {
        // given
        setUpContext(customerA);
        BigDecimal givenBalanceAccountA = BigDecimal.valueOf(1000);
        BigDecimal givenBalanceAccountB = BigDecimal.valueOf(0);
        BigDecimal givenTransferAmount = BigDecimal.valueOf(500);

        BankingAccount givenBankingAccountA = new BankingAccount(customerA);
        givenBankingAccountA.setId(2L);
        givenBankingAccountA.setBalance(givenBalanceAccountA);
        givenBankingAccountA.setAccountNumber("US9900001111112233334444");

        BankingAccount givenBankingAccountB = new BankingAccount(customerB);
        givenBankingAccountB.setId(5L);
        givenBankingAccountB.setBalance(givenBalanceAccountB);
        givenBankingAccountB.setAccountNumber("ES0400003110112293532124");

        BankingTransaction givenBankingTransactionA = new BankingTransaction(givenBankingAccountA);
        givenBankingTransactionA.setAmount(givenTransferAmount);

        BankingAccountTransactionRequest givenRequest = new BankingAccountTransactionRequest(
                givenBankingAccountB.getAccountNumber(),
                BankingTransactionType.TRANSFER_TO,
                "a gift!",
                BigDecimal.valueOf(500),
                RAW_PASSWORD
        );

        when(bankingAccountRepository.findById(givenBankingAccountA.getId())).thenReturn(Optional.of(
                givenBankingAccountA));
        when(bankingAccountRepository.findByAccountNumber(givenBankingAccountB.getAccountNumber()))
                .thenReturn(Optional.of(givenBankingAccountB));

        when(bankingTransactionService.createTransaction(
                any(BankingAccount.class),
                any(BankingTransactionType.class),
                any(BigDecimal.class),
                any(String.class)
        )).thenReturn(givenBankingTransactionA);

        when(bankingTransactionService.persistTransaction(
                any(BankingTransaction.class)
        )).thenReturn(givenBankingTransactionA);

        // then
        BankingTransaction transaction = bankingTransactionAccountService.processTransactionRequest(
                givenBankingAccountA.getId(),
                givenRequest
        );

        // then
        assertThat(transaction).isNotNull();
        assertThat(givenBankingAccountA.getBalance()).isEqualTo(
                givenBalanceAccountA.subtract(givenTransferAmount)
        );
        assertThat(givenBankingAccountB.getBalance()).isEqualTo(
                givenBalanceAccountB.add(givenTransferAmount)
        );
    }

    @Test
    @DisplayName("Should process transaction and fail to transfer when insufficient funds")
    void shouldProcessTransactionRequestAndFailToTransferWhenInsufficientFunds() {
        // given
        setUpContext(customerA);
        BigDecimal givenBalanceAccountA = BigDecimal.valueOf(0);
        BigDecimal givenBalanceAccountB = BigDecimal.valueOf(0);
        BigDecimal givenTransferAmount = BigDecimal.valueOf(500);

        BankingAccount givenBankingAccountA = new BankingAccount(customerA);
        givenBankingAccountA.setId(2L);
        givenBankingAccountA.setBalance(givenBalanceAccountA);
        givenBankingAccountA.setAccountNumber("US9900001111112233334444");

        BankingAccount givenBankingAccountB = new BankingAccount(customerB);
        givenBankingAccountB.setId(5L);
        givenBankingAccountB.setBalance(givenBalanceAccountB);
        givenBankingAccountB.setAccountNumber("ES0400003110112293532124");

        BankingTransaction givenBankingTransactionA = new BankingTransaction(givenBankingAccountA);
        givenBankingTransactionA.setAmount(givenTransferAmount);

        BankingAccountTransactionRequest givenRequest = new BankingAccountTransactionRequest(
                givenBankingAccountB.getAccountNumber(),
                BankingTransactionType.TRANSFER_TO,
                "a gift!",
                BigDecimal.valueOf(500),
                RAW_PASSWORD
        );

        when(bankingAccountRepository.findById(givenBankingAccountA.getId())).thenReturn(Optional.of(
                givenBankingAccountA));
        when(bankingAccountRepository.findByAccountNumber(givenBankingAccountB.getAccountNumber()))
                .thenReturn(Optional.of(givenBankingAccountB));

        // then
        BankingAccountAuthorizationException exception = assertThrows(
                BankingAccountAuthorizationException.class,
                () -> bankingTransactionAccountService.processTransactionRequest(
                        givenBankingAccountA.getId(),
                        givenRequest
                )
        );

        // then
        assertEquals(BankingAccountAuthorizationException.INSUFFICIENT_FUNDS, exception.getMessage());
    }

    @Test
    @DisplayName("Should process transaction and fail to transfer when account number is null")
    void shouldProcessTransactionRequestAndFailToTransferWhenAccountNumberIsNull() {
        // given
        //        setUpContext(customerA);
        BigDecimal givenBalanceAccountA = BigDecimal.valueOf(0);
        BigDecimal givenBalanceAccountB = BigDecimal.valueOf(0);
        BigDecimal givenTransferAmount = BigDecimal.valueOf(500);

        BankingAccount givenBankingAccountA = new BankingAccount(customerA);
        givenBankingAccountA.setId(2L);
        givenBankingAccountA.setBalance(givenBalanceAccountA);
        givenBankingAccountA.setAccountNumber("US9900001111112233334444");

        BankingAccount givenBankingAccountB = new BankingAccount(customerB);
        givenBankingAccountB.setId(5L);
        givenBankingAccountB.setBalance(givenBalanceAccountB);
        givenBankingAccountB.setAccountNumber(null);

        BankingTransaction givenBankingTransactionA = new BankingTransaction(givenBankingAccountA);
        givenBankingTransactionA.setAmount(givenTransferAmount);

        BankingAccountTransactionRequest givenRequest = new BankingAccountTransactionRequest(
                givenBankingAccountB.getAccountNumber(),
                BankingTransactionType.TRANSFER_TO,
                "a gift!",
                BigDecimal.valueOf(500),
                RAW_PASSWORD
        );

        when(bankingAccountRepository.findById(givenBankingAccountA.getId())).thenReturn(Optional.of(
                givenBankingAccountA));
        when(bankingAccountRepository.findByAccountNumber(givenBankingAccountB.getAccountNumber()))
                .thenReturn(Optional.empty());

        // then
        BankingAccountNotFoundException exception = assertThrows(
                BankingAccountNotFoundException.class,
                () -> bankingTransactionAccountService.processTransactionRequest(
                        givenBankingAccountA.getId(),
                        givenRequest
                )
        );

        // then
        assertEquals(BankingAccountNotFoundException.ACCOUNT_NOT_FOUND, exception.getMessage());
    }

    @Test
    @DisplayName("Should process transaction and fail to transfer when account is closed")
    void shouldProcessTransactionRequestAndFailToTransferWhenAccountIsClosed() {
        // given
        setUpContext(customerA);
        BigDecimal givenBalanceAccountA = BigDecimal.valueOf(0);
        BigDecimal givenBalanceAccountB = BigDecimal.valueOf(0);
        BigDecimal givenTransferAmount = BigDecimal.valueOf(500);

        BankingAccount givenBankingAccountA = new BankingAccount(customerA);
        givenBankingAccountA.setId(2L);
        givenBankingAccountA.setAccountStatus(BankingAccountStatus.CLOSED);
        givenBankingAccountA.setBalance(givenBalanceAccountA);
        givenBankingAccountA.setAccountNumber("US9900001111112233334444");

        BankingAccount givenBankingAccountB = new BankingAccount(customerB);
        givenBankingAccountB.setId(5L);
        givenBankingAccountB.setBalance(givenBalanceAccountB);
        givenBankingAccountB.setAccountNumber("ES0400003110112293532124");

        BankingTransaction givenBankingTransactionA = new BankingTransaction(givenBankingAccountA);
        givenBankingTransactionA.setAmount(givenTransferAmount);

        BankingAccountTransactionRequest givenRequest = new BankingAccountTransactionRequest(
                givenBankingAccountB.getAccountNumber(),
                BankingTransactionType.TRANSFER_TO,
                "a gift!",
                BigDecimal.valueOf(500),
                RAW_PASSWORD
        );

        when(bankingAccountRepository.findById(givenBankingAccountA.getId())).thenReturn(Optional.of(
                givenBankingAccountA));
        when(bankingAccountRepository.findByAccountNumber(givenBankingAccountB.getAccountNumber()))
                .thenReturn(Optional.of(givenBankingAccountB));

        // then
        BankingAccountAuthorizationException exception = assertThrows(
                BankingAccountAuthorizationException.class,
                () -> bankingTransactionAccountService.processTransactionRequest(
                        givenBankingAccountA.getId(),
                        givenRequest
                )
        );

        // then
        assertEquals(BankingAccountAuthorizationException.ACCOUNT_CLOSED, exception.getMessage());
    }

    @Test
    @DisplayName("Should process transaction and fail to transfer when account is suspended")
    void shouldProcessTransactionRequestAndFailToTransferWhenAccountIsSuspended() {
        // given
        setUpContext(customerA);
        BigDecimal givenBalanceAccountA = BigDecimal.valueOf(0);
        BigDecimal givenBalanceAccountB = BigDecimal.valueOf(0);
        BigDecimal givenTransferAmount = BigDecimal.valueOf(500);

        BankingAccount givenBankingAccountA = new BankingAccount(customerA);
        givenBankingAccountA.setId(2L);
        givenBankingAccountA.setAccountStatus(BankingAccountStatus.SUSPENDED);
        givenBankingAccountA.setBalance(givenBalanceAccountA);
        givenBankingAccountA.setAccountNumber("US9900001111112233334444");

        BankingAccount givenBankingAccountB = new BankingAccount(customerB);
        givenBankingAccountB.setId(5L);
        givenBankingAccountB.setBalance(givenBalanceAccountB);
        givenBankingAccountB.setAccountNumber("ES0400003110112293532124");

        BankingTransaction givenBankingTransactionA = new BankingTransaction(givenBankingAccountA);
        givenBankingTransactionA.setAmount(givenTransferAmount);

        BankingAccountTransactionRequest givenRequest = new BankingAccountTransactionRequest(
                givenBankingAccountB.getAccountNumber(),
                BankingTransactionType.TRANSFER_TO,
                "a gift!",
                BigDecimal.valueOf(500),
                RAW_PASSWORD
        );

        when(bankingAccountRepository.findById(givenBankingAccountA.getId())).thenReturn(Optional.of(
                givenBankingAccountA));
        when(bankingAccountRepository.findByAccountNumber(givenBankingAccountB.getAccountNumber()))
                .thenReturn(Optional.of(givenBankingAccountB));

        // then
        BankingAccountAuthorizationException exception = assertThrows(
                BankingAccountAuthorizationException.class,
                () -> bankingTransactionAccountService.processTransactionRequest(
                        givenBankingAccountA.getId(),
                        givenRequest
                )
        );

        // then
        assertEquals(BankingAccountAuthorizationException.ACCOUNT_SUSPENDED, exception.getMessage());
    }

    @Test
    @DisplayName("Should process transaction and fail to transfer when account is closed")
    void shouldProcessTransactionRequestAndFailToTransferWhenDestinyAccountIsClosed() {
        // given
        setUpContext(customerA);
        BigDecimal givenBalanceAccountA = BigDecimal.valueOf(0);
        BigDecimal givenBalanceAccountB = BigDecimal.valueOf(0);
        BigDecimal givenTransferAmount = BigDecimal.valueOf(500);

        BankingAccount givenBankingAccountA = new BankingAccount(customerA);
        givenBankingAccountA.setId(2L);
        givenBankingAccountA.setBalance(givenBalanceAccountA);
        givenBankingAccountA.setAccountNumber("US9900001111112233334444");

        BankingAccount givenBankingAccountB = new BankingAccount(customerB);
        givenBankingAccountB.setId(5L);
        givenBankingAccountB.setAccountStatus(BankingAccountStatus.CLOSED);
        givenBankingAccountB.setBalance(givenBalanceAccountB);
        givenBankingAccountB.setAccountNumber("ES0400003110112293532124");

        BankingTransaction givenBankingTransactionA = new BankingTransaction(givenBankingAccountA);
        givenBankingTransactionA.setAmount(givenTransferAmount);

        BankingAccountTransactionRequest givenRequest = new BankingAccountTransactionRequest(
                givenBankingAccountB.getAccountNumber(),
                BankingTransactionType.TRANSFER_TO,
                "a gift!",
                BigDecimal.valueOf(500),
                RAW_PASSWORD
        );

        when(bankingAccountRepository.findById(givenBankingAccountA.getId())).thenReturn(Optional.of(
                givenBankingAccountA));
        when(bankingAccountRepository.findByAccountNumber(givenBankingAccountB.getAccountNumber()))
                .thenReturn(Optional.of(givenBankingAccountB));

        // then
        BankingAccountAuthorizationException exception = assertThrows(
                BankingAccountAuthorizationException.class,
                () -> bankingTransactionAccountService.processTransactionRequest(
                        givenBankingAccountA.getId(),
                        givenRequest
                )
        );

        // then
        assertEquals(BankingAccountAuthorizationException.ACCOUNT_CLOSED, exception.getMessage());
    }

    @Test
    @DisplayName("Should process transaction and fail to transfer when account is suspended")
    void shouldProcessTransactionRequestAndFailToTransferWhenDestinyAccountIsSuspended() {
        // given
        setUpContext(customerA);
        BigDecimal givenBalanceAccountA = BigDecimal.valueOf(0);
        BigDecimal givenBalanceAccountB = BigDecimal.valueOf(0);
        BigDecimal givenTransferAmount = BigDecimal.valueOf(500);

        BankingAccount givenBankingAccountA = new BankingAccount(customerA);
        givenBankingAccountA.setId(2L);
        givenBankingAccountA.setBalance(givenBalanceAccountA);
        givenBankingAccountA.setAccountNumber("US9900001111112233334444");

        BankingAccount givenBankingAccountB = new BankingAccount(customerB);
        givenBankingAccountB.setId(5L);
        givenBankingAccountB.setBalance(givenBalanceAccountB);
        givenBankingAccountB.setAccountStatus(BankingAccountStatus.SUSPENDED);
        givenBankingAccountB.setAccountNumber("ES0400003110112293532124");

        BankingTransaction givenBankingTransactionA = new BankingTransaction(givenBankingAccountA);
        givenBankingTransactionA.setAmount(givenTransferAmount);

        BankingAccountTransactionRequest givenRequest = new BankingAccountTransactionRequest(
                givenBankingAccountB.getAccountNumber(),
                BankingTransactionType.TRANSFER_TO,
                "a gift!",
                BigDecimal.valueOf(500),
                RAW_PASSWORD
        );

        when(bankingAccountRepository.findById(givenBankingAccountA.getId())).thenReturn(Optional.of(
                givenBankingAccountA));
        when(bankingAccountRepository.findByAccountNumber(givenBankingAccountB.getAccountNumber()))
                .thenReturn(Optional.of(givenBankingAccountB));

        // then
        BankingAccountAuthorizationException exception = assertThrows(
                BankingAccountAuthorizationException.class,
                () -> bankingTransactionAccountService.processTransactionRequest(
                        givenBankingAccountA.getId(),
                        givenRequest
                )
        );

        // then
        assertEquals(BankingAccountAuthorizationException.ACCOUNT_SUSPENDED, exception.getMessage());
    }

    @Test
    @DisplayName("Should process transaction and fail to transfer to same account")
    void shouldProcessTransactionRequestAndFailToTransferWhenToSameAccount() {
        // given
        //        setUpContext(customerA);
        BigDecimal givenBalanceAccountA = BigDecimal.valueOf(0);
        BigDecimal givenBalanceAccountB = BigDecimal.valueOf(0);
        BigDecimal givenTransferAmount = BigDecimal.valueOf(500);

        BankingAccount givenBankingAccountA = new BankingAccount(customerA);
        givenBankingAccountA.setId(2L);
        givenBankingAccountA.setBalance(givenBalanceAccountA);
        givenBankingAccountA.setAccountNumber("US9900001111112233334444");

        BankingTransaction givenBankingTransactionA = new BankingTransaction(givenBankingAccountA);
        givenBankingTransactionA.setAmount(givenTransferAmount);

        BankingAccountTransactionRequest givenRequest = new BankingAccountTransactionRequest(
                givenBankingAccountA.getAccountNumber(),
                BankingTransactionType.TRANSFER_TO,
                "a gift!",
                BigDecimal.valueOf(500),
                RAW_PASSWORD
        );

        when(bankingAccountRepository.findById(givenBankingAccountA.getId())).thenReturn(Optional.of(
                givenBankingAccountA));
        when(bankingAccountRepository.findByAccountNumber(givenBankingAccountA.getAccountNumber()))
                .thenReturn(Optional.of(givenBankingAccountA));

        // then
        BankingAccountAuthorizationException exception = assertThrows(
                BankingAccountAuthorizationException.class,
                () -> bankingTransactionAccountService.processTransactionRequest(
                        givenBankingAccountA.getId(),
                        givenRequest
                )
        );

        // then
        assertEquals(BankingAccountAuthorizationException.TRANSFER_TO_SAME_ACCOUNT, exception.getMessage());
    }

    @Test
    @DisplayName("Should process transaction and fail to transfer when account its not yours")
    void shouldProcessTransactionRequestAndFailToTransferWhenAccountItsNotYours() {
        // given
        setUpContext(customerA);
        BigDecimal givenBalanceAccountA = BigDecimal.valueOf(0);
        BigDecimal givenBalanceAccountB = BigDecimal.valueOf(0);
        BigDecimal givenTransferAmount = BigDecimal.valueOf(500);

        BankingAccount givenBankingAccountA = new BankingAccount(customerB);
        givenBankingAccountA.setId(2L);
        givenBankingAccountA.setBalance(givenBalanceAccountA);
        givenBankingAccountA.setAccountNumber("US9900001111112233334444");

        BankingAccount givenBankingAccountB = new BankingAccount(customerA);
        givenBankingAccountB.setId(5L);
        givenBankingAccountB.setBalance(givenBalanceAccountB);
        givenBankingAccountB.setAccountNumber("ES0400003110112293532124");

        BankingTransaction givenBankingTransactionA = new BankingTransaction(givenBankingAccountA);
        givenBankingTransactionA.setAmount(givenTransferAmount);

        BankingAccountTransactionRequest givenRequest = new BankingAccountTransactionRequest(
                givenBankingAccountB.getAccountNumber(),
                BankingTransactionType.TRANSFER_TO,
                "a gift!",
                BigDecimal.valueOf(500),
                RAW_PASSWORD
        );

        when(bankingAccountRepository.findById(givenBankingAccountA.getId())).thenReturn(Optional.of(
                givenBankingAccountA));
        when(bankingAccountRepository.findByAccountNumber(givenBankingAccountB.getAccountNumber()))
                .thenReturn(Optional.of(givenBankingAccountB));

        // then
        BankingAccountAuthorizationException exception = assertThrows(
                BankingAccountAuthorizationException.class,
                () -> bankingTransactionAccountService.processTransactionRequest(
                        givenBankingAccountA.getId(),
                        givenRequest
                )
        );

        // then
        assertEquals(BankingAccountAuthorizationException.ACCOUNT_NOT_BELONG_TO_CUSTOMER, exception.getMessage());
    }

    @Test
    @DisplayName("Should process transaction and fail to transfer when password is wrong")
    void shouldProcessTransactionRequestAndFailToTransferWhenPasswordIsWrong() {
        // given
        setUpContext(customerA);
        BigDecimal givenBalanceAccountA = BigDecimal.valueOf(0);
        BigDecimal givenBalanceAccountB = BigDecimal.valueOf(0);
        BigDecimal givenTransferAmount = BigDecimal.valueOf(500);

        BankingAccount givenBankingAccountA = new BankingAccount(customerA);
        givenBankingAccountA.setId(2L);
        givenBankingAccountA.setBalance(givenBalanceAccountA);
        givenBankingAccountA.setAccountNumber("US9900001111112233334444");

        BankingAccount givenBankingAccountB = new BankingAccount(customerB);
        givenBankingAccountB.setId(5L);
        givenBankingAccountB.setBalance(givenBalanceAccountB);
        givenBankingAccountB.setAccountNumber("ES0400003110112293532124");

        BankingTransaction givenBankingTransactionA = new BankingTransaction(givenBankingAccountA);
        givenBankingTransactionA.setAmount(givenTransferAmount);

        BankingAccountTransactionRequest givenRequest = new BankingAccountTransactionRequest(
                givenBankingAccountB.getAccountNumber(),
                BankingTransactionType.TRANSFER_TO,
                "a gift!",
                BigDecimal.valueOf(500),
                "WRONG_PASSWORD"
        );

        when(bankingAccountRepository.findById(givenBankingAccountA.getId())).thenReturn(Optional.of(
                givenBankingAccountA));
        when(bankingAccountRepository.findByAccountNumber(givenBankingAccountB.getAccountNumber()))
                .thenReturn(Optional.of(givenBankingAccountB));

        // then
        PasswordMismatchException exception = assertThrows(
                PasswordMismatchException.class,
                () -> bankingTransactionAccountService.processTransactionRequest(
                        givenBankingAccountA.getId(),
                        givenRequest
                )
        );

        // then
        assertEquals(PasswordMismatchException.PASSWORD_MISMATCH, exception.getMessage());
    }

}