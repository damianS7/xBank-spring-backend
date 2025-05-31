package com.damian.xBank.banking.card;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.card.exception.BankingCardAuthorizationException;
import com.damian.xBank.banking.transactions.*;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BankingCardTransactionServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BankingTransactionService bankingTransactionService;

    @InjectMocks
    private BankingTransactionController.BankingCardUsageService bankingCardUsageService;

    private Customer customerA;
    private Customer customerB;
    private Customer customerAdmin;

    @BeforeEach
    void setUp() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        customerRepository.deleteAll();
        customerA = new Customer(99L, "customerA@test.com", bCryptPasswordEncoder.encode("123456"));
        customerB = new Customer(92L, "customerB@test.com", bCryptPasswordEncoder.encode("123456"));
        customerAdmin = new Customer(95L, "admin@test.com", bCryptPasswordEncoder.encode("123456"));
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
        Mockito.when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(customer);
    }

    // TODO spendRequest
    @Test
    @DisplayName("Should spend")
    void shouldSpendRequest() {

    }

    // TODO withdrawal and withdrawalRequest

    @Test
    @DisplayName("Should spend")
    void shouldSpend() {
        // given
        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setBalance(BigDecimal.valueOf(1000));
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        BankingTransaction givenBankingTransaction = new BankingTransaction(givenBankAccount);
        givenBankingTransaction.setTransactionType(BankingTransactionType.CARD_CHARGE);
        givenBankingTransaction.setAmount(BigDecimal.valueOf(100));
        givenBankingTransaction.setDescription("Amazon.com");

        when(bankingTransactionService.createTransaction(
                any(BankingCard.class),
                any(BankingTransactionType.class),
                any(BigDecimal.class),
                any(String.class)
        )).thenReturn(givenBankingTransaction);

        when(bankingTransactionService.persistTransaction(
                any(BankingTransaction.class)
        )).thenReturn(givenBankingTransaction);

        // then
        BankingTransaction transaction = bankingCardUsageService.spend(
                givenBankingCard,
                givenBankingTransaction.getAmount(),
                givenBankingTransaction.getDescription()
        );

        // then
        assertThat(transaction).isNotNull();
        assertThat(transaction.getTransactionType()).isEqualTo(givenBankingTransaction.getTransactionType());
        assertThat(transaction.getDescription()).isEqualTo(givenBankingTransaction.getDescription());
        assertThat(transaction.getTransactionStatus()).isEqualTo(BankingTransactionStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should not spend when card is disabled")
    void shouldNotSpendWhenCardIsDisabled() {
        // given
        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setBalance(BigDecimal.valueOf(1000));
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.DISABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        // then
        BankingCardAuthorizationException exception = assertThrows(
                BankingCardAuthorizationException.class,
                () -> bankingCardUsageService.spend(givenBankingCard, BigDecimal.valueOf(100), "Amazon.com")
        );

        // then
        assertTrue(exception.getMessage().contains("The card is disabled."));
    }

    @Test
    @DisplayName("Should not spend when card is locked")
    void shouldNotSpendWhenCardIsLocked() {
        // given
        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setBalance(BigDecimal.valueOf(1000));
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);
        givenBankingCard.setLockStatus(BankingCardLockStatus.LOCKED);

        // then
        BankingCardAuthorizationException exception = assertThrows(
                BankingCardAuthorizationException.class,
                () -> bankingCardUsageService.spend(givenBankingCard, BigDecimal.valueOf(100), "Amazon.com")
        );

        // then
        assertTrue(exception.getMessage().contains("The card is locked."));
    }

    @Test
    @DisplayName("Should not spend when  insufficient funds")
    void shouldSpendWhenInsufficientFunds() {
        // given
        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setBalance(BigDecimal.valueOf(0));
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        BankingTransaction givenBankingTransaction = new BankingTransaction(givenBankAccount);
        givenBankingTransaction.setTransactionType(BankingTransactionType.CARD_CHARGE);
        givenBankingTransaction.setAmount(BigDecimal.valueOf(100));
        givenBankingTransaction.setDescription("Amazon.com");

        when(bankingTransactionService.createTransaction(
                any(BankingCard.class),
                any(BankingTransactionType.class),
                any(BigDecimal.class),
                any(String.class)
        )).thenReturn(givenBankingTransaction);

        // then
        BankingCardAuthorizationException exception = assertThrows(
                BankingCardAuthorizationException.class,
                () -> bankingCardUsageService.spend(
                        givenBankingCard,
                        givenBankingTransaction.getAmount(),
                        givenBankingTransaction.getDescription()
                )
        );

        // then
        assertTrue(exception.getMessage().contains("Insufficient funds."));
    }


}
