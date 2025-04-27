package com.damian.xBank.banking.card;

import com.damian.xBank.banking.account.*;
import com.damian.xBank.banking.account.exception.BankingAccountAuthorizationException;
import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
import com.damian.xBank.banking.card.exception.BankingCardAuthorizationException;
import com.damian.xBank.banking.card.http.BankingCardCreateRequest;
import com.damian.xBank.banking.transactions.BankingTransactionType;
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
public class BankingCardServiceTest {

    @Mock
    private BankingAccountRepository bankingAccountRepository;

    @Mock
    private BankingCardRepository bankingCardRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private Faker faker;

    @Mock
    private Finance finance;

    @InjectMocks
    private BankingAccountService bankingAccountService;

    @InjectMocks
    private BankingCardService bankingCardService;

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
    @DisplayName("Should generate a BankingCard")
    void shouldGenerateBankingCard() {
        // given
        setUpContext(customerA);

        BankingCardCreateRequest request = new BankingCardCreateRequest(BankingCardType.CREDIT);

        final String accountNumber = "US99 0000 1111 1122 3333 4444";

        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber(accountNumber);

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setLinkedBankingAccount(givenBankAccount);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardType(BankingCardType.CREDIT);
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);

        // when
        when(faker.finance()).thenReturn(finance);
        when(finance.creditCard()).thenReturn(givenBankingCard.getCardNumber());
        when(bankingAccountRepository.findById(anyLong())).thenReturn(Optional.of(givenBankAccount));
        when(bankingCardRepository.save(any(BankingCard.class))).thenReturn(givenBankingCard);

        BankingCard savedCard = bankingCardService.createCard(givenBankAccount.getId(), request);

        // then
        assertThat(savedCard.getCardNumber()).isEqualTo(givenBankingCard.getCardNumber());
        assertThat(savedCard.getCardType()).isEqualTo(givenBankingCard.getCardType());
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should generate a BankingCard when account is not yours but you are admin")
    void shouldGenerateBankingCardWhenAccountIsNotYoursButYouAreAdmin() {
        // given
        setUpContext(customerAdmin);

        BankingCardCreateRequest request = new BankingCardCreateRequest(BankingCardType.CREDIT);

        final String accountNumber = "US99 0000 1111 1122 3333 4444";

        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber(accountNumber);

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setLinkedBankingAccount(givenBankAccount);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardType(BankingCardType.CREDIT);
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);

        // when
        when(faker.finance()).thenReturn(finance);
        when(finance.creditCard()).thenReturn(givenBankingCard.getCardNumber());
        when(bankingAccountRepository.findById(anyLong())).thenReturn(Optional.of(givenBankAccount));
        when(bankingCardRepository.save(any(BankingCard.class))).thenReturn(givenBankingCard);

        BankingCard savedCard = bankingCardService.createCard(givenBankAccount.getId(), request);

        // then
        assertThat(savedCard.getCardNumber()).isEqualTo(givenBankingCard.getCardNumber());
        assertThat(savedCard.getCardType()).isEqualTo(givenBankingCard.getCardType());
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should not generate a BankingCard when BankingAccount is not yours")
    void shouldNotGenerateBankingCardWhenBankingAccountIsNotYours() {
        // given
        setUpContext(customerA);

        BankingCardCreateRequest request = new BankingCardCreateRequest(BankingCardType.CREDIT);

        final String accountNumber = "US99 0000 1111 1122 3333 4444";

        BankingAccount givenBankAccount = new BankingAccount(customerB);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber(accountNumber);

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setLinkedBankingAccount(givenBankAccount);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardType(BankingCardType.CREDIT);
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);

        // when
        when(bankingAccountRepository.findById(anyLong())).thenReturn(Optional.of(givenBankAccount));

        BankingAccountAuthorizationException exception = assertThrows(
                BankingAccountAuthorizationException.class,
                () -> bankingCardService.createCard(givenBankAccount.getId(), request)
        );

        // then
        assertTrue(exception.getMessage().contains("You are not the owner of this account."));
    }

    @Test
    @DisplayName("Should cancel a BankingCard")
    void shouldCancelBankingCard() {
        // given
        setUpContext(customerA);

        final String accountNumber = "US99 0000 1111 1122 3333 4444";

        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber(accountNumber);

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setLinkedBankingAccount(givenBankAccount);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardType(BankingCardType.CREDIT);
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
        when(bankingCardRepository.save(any(BankingCard.class))).thenReturn(givenBankingCard);

        BankingCard savedCard = bankingCardService.cancelCard(givenBankAccount.getId());

        // then
        assertThat(savedCard.getCardNumber()).isEqualTo(givenBankingCard.getCardNumber());
        assertThat(savedCard.getCardType()).isEqualTo(givenBankingCard.getCardType());
        assertThat(savedCard.getCardStatus()).isEqualTo(BankingCardStatus.DISABLED);
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should cancel a BankingCard when its not yours but you are admin")
    void shouldCancelBankingCardWhenItsNotYoursButYouAreAdmin() {
        // given
        setUpContext(customerAdmin);

        final String accountNumber = "US99 0000 1111 1122 3333 4444";

        BankingAccount givenBankAccount = new BankingAccount(customerB);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber(accountNumber);

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setLinkedBankingAccount(givenBankAccount);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardType(BankingCardType.CREDIT);
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
        when(bankingCardRepository.save(any(BankingCard.class))).thenReturn(givenBankingCard);

        BankingCard savedCard = bankingCardService.cancelCard(givenBankAccount.getId());

        // then
        assertThat(savedCard.getCardNumber()).isEqualTo(givenBankingCard.getCardNumber());
        assertThat(savedCard.getCardType()).isEqualTo(givenBankingCard.getCardType());
        assertThat(savedCard.getCardStatus()).isEqualTo(BankingCardStatus.DISABLED);
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should not cancel a BankingCard when its not yours")
    void shouldNotCancelBankingCardWhenItsNotYours() {
        // given
        setUpContext(customerA);

        final String accountNumber = "US99 0000 1111 1122 3333 4444";

        BankingAccount givenBankAccount = new BankingAccount(customerB);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber(accountNumber);

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setLinkedBankingAccount(givenBankAccount);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardType(BankingCardType.CREDIT);
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));

        // then
        BankingCardAuthorizationException exception = assertThrows(
                BankingCardAuthorizationException.class,
                () -> bankingCardService.cancelCard(givenBankAccount.getId())
        );

        // then
        assertTrue(exception.getMessage().contains("You are not the owner of this card."));
    }

    @Test
    @DisplayName("Should not spend when card is blocked")
    void shouldNotSpendWhenCardIsBlocked() {
        // given
        setUpContext(customerA);

        final String accountNumber = "US99 0000 1111 1122 3333 4444";

        final BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
                null,
                BigDecimal.valueOf(100),
                BankingTransactionType.CARD_CHARGE,
                "Amazon.com"
        );

        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber(accountNumber);

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setLinkedBankingAccount(givenBankAccount);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardType(BankingCardType.CREDIT);
        givenBankingCard.setCardStatus(BankingCardStatus.LOCKED);

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));

        // then
        BankingCardAuthorizationException exception = assertThrows(
                BankingCardAuthorizationException.class,
                () -> bankingCardService.spend(givenBankAccount.getId(), request)
        );

        // then
        assertTrue(exception.getMessage().contains("The card is locked."));
    }

    @Test
    @DisplayName("Should not spend when card is disabled")
    void shouldNotSpendWhenCardIsDisabled() {
        // given
        setUpContext(customerA);

        final String accountNumber = "US99 0000 1111 1122 3333 4444";

        final BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
                null,
                BigDecimal.valueOf(100),
                BankingTransactionType.CARD_CHARGE,
                "Amazon.com"
        );

        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber(accountNumber);

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setLinkedBankingAccount(givenBankAccount);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardType(BankingCardType.CREDIT);
        givenBankingCard.setCardStatus(BankingCardStatus.DISABLED);

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));

        // then
        BankingCardAuthorizationException exception = assertThrows(
                BankingCardAuthorizationException.class,
                () -> bankingCardService.spend(givenBankAccount.getId(), request)
        );

        // then
        assertTrue(exception.getMessage().contains("The card is disabled."));
    }
}
