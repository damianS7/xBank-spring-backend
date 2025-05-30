package com.damian.xBank.banking.card;

import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountRepository;
import com.damian.xBank.banking.account.BankingAccountService;
import com.damian.xBank.banking.transactions.BankingTransaction;
import com.damian.xBank.banking.transactions.BankingTransactionType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankingCardUsageServiceTest {

    @Mock
    private BankingAccountRepository bankingAccountRepository;

    @Mock
    private BankingCardRepository bankingCardRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private BankingAccountService bankingAccountService;

    @InjectMocks
    private BankingCardUsageService bankingCardUsageService;

    @InjectMocks
    private BankingCardService bankingCardService;

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
    
    @Test
    @DisplayName("Should spend")
    void shouldSpend() {
        // given
        //        setUpContext(customerA);

        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setBalance(BigDecimal.valueOf(1000));
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
        when(bankingCardRepository.save(any(BankingCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // then
        BankingTransaction transaction = bankingCardUsageService.spend(
                givenBankingCard,
                BigDecimal.valueOf(100),
                "Amazon.com"
        );

        // then
        assertThat(transaction).isNotNull();
        assertThat(transaction.getTransactionType()).isEqualTo(BankingTransactionType.CARD_CHARGE);
        assertThat(givenBankingCard.getBalance()).isEqualTo(BigDecimal.valueOf(900));
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));

    }

    //    @Test
    //    @DisplayName("Should not spend when card is blocked")
    //    void shouldNotSpendWhenCardIsBlocked() {
    //        // given
    //        setUpContext(customerA);
    //
    //        final String accountNumber = "US99 0000 1111 1122 3333 4444";
    //
    //        final BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
    //                null,
    //                BigDecimal.valueOf(100),
    //                BankingTransactionType.CARD_CHARGE,
    //                "Amazon.com"
    //        );
    //
    //        BankingAccount givenBankAccount = new BankingAccount(customerA);
    //        givenBankAccount.setId(5L);
    //        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
    //        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
    //        givenBankAccount.setAccountNumber(accountNumber);
    //
    //        BankingCard givenBankingCard = new BankingCard();
    //        givenBankingCard.setId(11L);
    //        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);
    //        givenBankingCard.setCardNumber("1234567890123456");
    //        givenBankingCard.setCardType(BankingCardType.CREDIT);
    //        givenBankingCard.setLockStatus(BankingCardLockStatus.LOCKED);
    //
    //        // when
    //        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
    //
    //        // then
    //        BankingCardAuthorizationException exception = assertThrows(
    //                BankingCardAuthorizationException.class,
    //                () -> bankingCardUsageService.spend(givenBankAccount.getId(), request)
    //        );
    //
    //        // then
    //        assertTrue(exception.getMessage().contains("The card is locked."));
    //    }
    //
    //    @Test
    //    @DisplayName("Should not spend when card is disabled")
    //    void shouldNotSpendWhenCardIsDisabled() {
    //        // given
    //        setUpContext(customerA);
    //
    //        final String accountNumber = "US99 0000 1111 1122 3333 4444";
    //
    //        final BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
    //                null,
    //                BigDecimal.valueOf(100),
    //                BankingTransactionType.CARD_CHARGE,
    //                "Amazon.com"
    //        );
    //
    //        BankingAccount givenBankAccount = new BankingAccount(customerA);
    //        givenBankAccount.setId(5L);
    //        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
    //        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
    //        givenBankAccount.setAccountNumber(accountNumber);
    //
    //        BankingCard givenBankingCard = new BankingCard();
    //        givenBankingCard.setId(11L);
    //        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);
    //        givenBankingCard.setCardNumber("1234567890123456");
    //        givenBankingCard.setCardType(BankingCardType.CREDIT);
    //        givenBankingCard.setCardStatus(BankingCardStatus.DISABLED);
    //
    //        // when
    //        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
    //
    //        // then
    //        BankingCardAuthorizationException exception = assertThrows(
    //                BankingCardAuthorizationException.class,
    //                () -> bankingCardUsageService.spend(givenBankAccount.getId(), request)
    //        );
    //
    //        // then
    //        assertTrue(exception.getMessage().contains("The card is disabled."));
    //    }
    //
    //    @Test
    //    @DisplayName("Should not spend when card is disabled")
    //    void shouldNotSpendWhenCardIsNotYours() {
    //        // given
    //        setUpContext(customerA);
    //
    //        final String accountNumber = "US99 0000 1111 1122 3333 4444";
    //
    //        final BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
    //                null,
    //                BigDecimal.valueOf(100),
    //                BankingTransactionType.CARD_CHARGE,
    //                "Amazon.com"
    //        );
    //
    //        BankingAccount givenBankAccount = new BankingAccount(customerA);
    //        givenBankAccount.setId(5L);
    //        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
    //        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
    //        givenBankAccount.setAccountNumber(accountNumber);
    //
    //        BankingCard givenBankingCard = new BankingCard();
    //        givenBankingCard.setId(11L);
    //        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);
    //        givenBankingCard.setCardNumber("1234567890123456");
    //        givenBankingCard.setCardType(BankingCardType.CREDIT);
    //        givenBankingCard.setCardStatus(BankingCardStatus.DISABLED);
    //
    //        // when
    //        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
    //
    //        // then
    //        BankingCardAuthorizationException exception = assertThrows(
    //                BankingCardAuthorizationException.class,
    //                () -> bankingCardUsageService.spend(givenBankAccount.getId(), request)
    //        );
    //
    //        // then
    //        assertTrue(exception.getMessage().contains("The card is disabled."));
    //    }
    //
    //    @Test
    //    @DisplayName("Should not spend when card is disabled")
    //    void shouldNotSpendWhenCardNotExists() {
    //        // given
    //        setUpContext(customerA);
    //
    //        final String accountNumber = "US99 0000 1111 1122 3333 4444";
    //
    //        final BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
    //                null,
    //                BigDecimal.valueOf(100),
    //                BankingTransactionType.CARD_CHARGE,
    //                "Amazon.com"
    //        );
    //
    //        BankingAccount givenBankAccount = new BankingAccount(customerA);
    //        givenBankAccount.setId(5L);
    //        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
    //        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
    //        givenBankAccount.setAccountNumber(accountNumber);
    //
    //        BankingCard givenBankingCard = new BankingCard();
    //        givenBankingCard.setId(11L);
    //        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);
    //        givenBankingCard.setCardNumber("1234567890123456");
    //        givenBankingCard.setCardType(BankingCardType.CREDIT);
    //        givenBankingCard.setCardStatus(BankingCardStatus.DISABLED);
    //
    //        // when
    //        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
    //
    //        // then
    //        BankingCardAuthorizationException exception = assertThrows(
    //                BankingCardAuthorizationException.class,
    //                () -> bankingCardUsageService.spend(givenBankAccount.getId(), request)
    //        );
    //
    //        // then
    //        assertTrue(exception.getMessage().contains("The card is disabled."));
    //    }
    //
    //    @Test
    //    @DisplayName("Should not spend when card is blocked")
    //    void shouldNotSpendWhenCardHasNoFunds() {
    //        // given
    //        setUpContext(customerA);
    //
    //        final String accountNumber = "US99 0000 1111 1122 3333 4444";
    //
    //        final BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
    //                null,
    //                BigDecimal.valueOf(100),
    //                BankingTransactionType.CARD_CHARGE,
    //                "Amazon.com"
    //        );
    //
    //        BankingAccount givenBankAccount = new BankingAccount(customerA);
    //        givenBankAccount.setId(5L);
    //        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
    //        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
    //        givenBankAccount.setAccountNumber(accountNumber);
    //
    //        BankingCard givenBankingCard = new BankingCard();
    //        givenBankingCard.setId(11L);
    //        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);
    //        givenBankingCard.setCardNumber("1234567890123456");
    //        givenBankingCard.setCardType(BankingCardType.CREDIT);
    //        givenBankingCard.setLockStatus(BankingCardLockStatus.LOCKED);
    //
    //        // when
    //        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
    //
    //        // then
    //        BankingCardAuthorizationException exception = assertThrows(
    //                BankingCardAuthorizationException.class,
    //                () -> bankingCardUsageService.spend(givenBankAccount.getId(), request)
    //        );
    //
    //        // then
    //        assertTrue(exception.getMessage().contains("The card is locked."));
    //    }
}
