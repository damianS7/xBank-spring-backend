package com.damian.xBank.banking.transaction;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.card.BankingCard;
import com.damian.xBank.banking.card.BankingCardLockStatus;
import com.damian.xBank.banking.card.BankingCardRepository;
import com.damian.xBank.banking.card.BankingCardStatus;
import com.damian.xBank.banking.card.exception.BankingCardAuthorizationException;
import com.damian.xBank.banking.card.exception.BankingCardNotFoundException;
import com.damian.xBank.banking.transactions.*;
import com.damian.xBank.banking.transactions.http.BankingCardTransactionRequest;
import com.damian.xBank.common.exception.Exceptions;
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
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BankingTransactionCardServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BankingCardRepository bankingCardRepository;

    @Mock
    private BankingTransactionService bankingTransactionService;

    @InjectMocks
    private BankingTransactionCardService bankingTransactionCardService;

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
        Mockito.when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
        Mockito.when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(customer);
    }

    @Test
    @DisplayName("Should process card transaction and spend")
    void shouldProcessTransactionRequestAndSpend() {
        // given
        setUpContext(customerA);

        BigDecimal givenBalance = new BigDecimal("1000");

        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setBalance(givenBalance);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardPin("1234");
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        BankingTransaction givenBankingTransaction = new BankingTransaction(givenBankAccount);
        givenBankingTransaction.setTransactionType(BankingTransactionType.CARD_CHARGE);
        givenBankingTransaction.setAmount(BigDecimal.valueOf(100));
        givenBankingTransaction.setDescription("Amazon.com");

        BankingCardTransactionRequest givenRequest = new BankingCardTransactionRequest(
                BankingTransactionType.CARD_CHARGE,
                "Amazon.com",
                BigDecimal.valueOf(100),
                givenBankingCard.getCardPin()
        );

        when(bankingCardRepository.findById(givenBankingCard.getId())).thenReturn(Optional.of(givenBankingCard));
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
        BankingTransaction transaction = bankingTransactionCardService.processTransactionRequest(
                givenBankingCard.getId(),
                givenRequest
        );

        // then
        assertThat(transaction).isNotNull();
        assertThat(transaction.getTransactionType()).isEqualTo(givenBankingTransaction.getTransactionType());
        assertThat(transaction.getDescription()).isEqualTo(givenBankingTransaction.getDescription());
        assertThat(transaction.getTransactionStatus()).isEqualTo(BankingTransactionStatus.COMPLETED);
        assertThat(givenBankingCard.getBalance()).isEqualTo(
                givenBalance.subtract(givenRequest.amount())
        );
    }

    @Test
    @DisplayName("Should fail to process card transaction when card not found")
    void shouldFailToProcessCardTransactionWhenCardNotFoundRequest() {
        // given
        BankingCardTransactionRequest givenRequest = new BankingCardTransactionRequest(
                BankingTransactionType.CARD_CHARGE,
                "Amazon.com",
                BigDecimal.valueOf(100),
                "1234"
        );

        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.empty());

        // then
        BankingCardNotFoundException exception = assertThrows(
                BankingCardNotFoundException.class,
                () -> bankingTransactionCardService.processTransactionRequest(
                        1L,
                        givenRequest
                )
        );

        // then
        assertTrue(exception.getMessage().contains(Exceptions.CARD.NOT_FOUND));
    }

    @Test
    @DisplayName("Should fail to process card transaction when card not belong to customer")
    void shouldFailToProcessCardTransactionWhenCardNotBelongToCustomerRequest() {
        // given
        setUpContext(customerA);

        BigDecimal givenBalance = new BigDecimal("1000");

        BankingAccount givenBankAccount = new BankingAccount(customerB);
        givenBankAccount.setId(5L);
        givenBankAccount.setBalance(givenBalance);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardPin("1234");
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        BankingTransaction givenBankingTransaction = new BankingTransaction(givenBankAccount);
        givenBankingTransaction.setTransactionType(BankingTransactionType.CARD_CHARGE);
        givenBankingTransaction.setAmount(BigDecimal.valueOf(100));
        givenBankingTransaction.setDescription("Amazon.com");

        BankingCardTransactionRequest givenRequest = new BankingCardTransactionRequest(
                BankingTransactionType.CARD_CHARGE,
                "Amazon.com",
                BigDecimal.valueOf(100),
                givenBankingCard.getCardPin()
        );

        when(bankingCardRepository.findById(givenBankingCard.getId())).thenReturn(Optional.of(givenBankingCard));

        // then
        BankingCardAuthorizationException exception = assertThrows(
                BankingCardAuthorizationException.class,
                () -> bankingTransactionCardService.processTransactionRequest(
                        givenBankingCard.getId(),
                        givenRequest
                )
        );

        // then
        assertTrue(exception.getMessage().contains(Exceptions.CARD.ACCESS_FORBIDDEN));
    }

    @Test
    @DisplayName("Should fail to process card transaction when card is disabled")
    void shouldFailToProcessCardTransactionWhenCardIsDisabledRequest() {
        // given
        setUpContext(customerA);

        BigDecimal givenBalance = new BigDecimal("1000");

        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setBalance(givenBalance);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardPin("1234");
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.DISABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        BankingTransaction givenBankingTransaction = new BankingTransaction(givenBankAccount);
        givenBankingTransaction.setTransactionType(BankingTransactionType.CARD_CHARGE);
        givenBankingTransaction.setAmount(BigDecimal.valueOf(100));
        givenBankingTransaction.setDescription("Amazon.com");

        BankingCardTransactionRequest givenRequest = new BankingCardTransactionRequest(
                BankingTransactionType.CARD_CHARGE,
                "Amazon.com",
                BigDecimal.valueOf(100),
                givenBankingCard.getCardPin()
        );

        when(bankingCardRepository.findById(givenBankingCard.getId())).thenReturn(Optional.of(givenBankingCard));

        // then
        BankingCardAuthorizationException exception = assertThrows(
                BankingCardAuthorizationException.class,
                () -> bankingTransactionCardService.processTransactionRequest(
                        givenBankingCard.getId(),
                        givenRequest
                )
        );

        // then
        assertTrue(exception.getMessage().contains(Exceptions.CARD.DISABLED));
    }

    @Test
    @DisplayName("Should fail to process card transaction when card is locked")
    void shouldFailToProcessCardTransactionWhenCardIsLockedRequest() {
        // given
        setUpContext(customerA);

        BigDecimal givenBalance = new BigDecimal("1000");

        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setBalance(givenBalance);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardPin("1234");
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setLockStatus(BankingCardLockStatus.LOCKED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        BankingTransaction givenBankingTransaction = new BankingTransaction(givenBankAccount);
        givenBankingTransaction.setTransactionType(BankingTransactionType.CARD_CHARGE);
        givenBankingTransaction.setAmount(BigDecimal.valueOf(100));
        givenBankingTransaction.setDescription("Amazon.com");

        BankingCardTransactionRequest givenRequest = new BankingCardTransactionRequest(
                BankingTransactionType.CARD_CHARGE,
                "Amazon.com",
                BigDecimal.valueOf(100),
                givenBankingCard.getCardPin()
        );

        when(bankingCardRepository.findById(givenBankingCard.getId())).thenReturn(Optional.of(givenBankingCard));

        // then
        BankingCardAuthorizationException exception = assertThrows(
                BankingCardAuthorizationException.class,
                () -> bankingTransactionCardService.processTransactionRequest(
                        givenBankingCard.getId(),
                        givenRequest
                )
        );

        // then
        assertTrue(exception.getMessage().contains(Exceptions.CARD.LOCKED));
    }

    @Test
    @DisplayName("Should fail to process card transaction when card funds are insufficient")
    void shouldFailToProcessCardTransactionWhenCardFundsAreInsufficientRequest() {
        // given
        setUpContext(customerA);

        BigDecimal givenBalance = new BigDecimal("0");

        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setBalance(givenBalance);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardPin("1234");
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        BankingTransaction givenBankingTransaction = new BankingTransaction(givenBankAccount);
        givenBankingTransaction.setTransactionType(BankingTransactionType.CARD_CHARGE);
        givenBankingTransaction.setAmount(BigDecimal.valueOf(100));
        givenBankingTransaction.setDescription("Amazon.com");

        BankingCardTransactionRequest givenRequest = new BankingCardTransactionRequest(
                BankingTransactionType.CARD_CHARGE,
                "Amazon.com",
                BigDecimal.valueOf(100),
                givenBankingCard.getCardPin()
        );

        when(bankingCardRepository.findById(givenBankingCard.getId())).thenReturn(Optional.of(givenBankingCard));

        // then
        BankingCardAuthorizationException exception = assertThrows(
                BankingCardAuthorizationException.class,
                () -> bankingTransactionCardService.processTransactionRequest(
                        givenBankingCard.getId(),
                        givenRequest
                )
        );

        // then
        assertTrue(exception.getMessage().contains(Exceptions.CARD.INSUFFICIENT_FUNDS));
    }

}
