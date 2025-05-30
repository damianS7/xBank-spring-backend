package com.damian.xBank.banking.card;

import com.damian.xBank.auth.http.PasswordConfirmationRequest;
import com.damian.xBank.banking.account.*;
import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
import com.damian.xBank.banking.card.exception.BankingCardAuthorizationException;
import com.damian.xBank.banking.card.exception.BankingCardNotFoundException;
import com.damian.xBank.banking.card.http.BankingCardSetDailyLimitRequest;
import com.damian.xBank.banking.card.http.BankingCardSetPinRequest;
import com.damian.xBank.banking.transactions.BankingTransactionType;
import com.damian.xBank.common.exception.PasswordMismatchException;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import net.datafaker.Faker;
import net.datafaker.providers.base.Finance;
import net.datafaker.providers.base.Number;
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
    @DisplayName("Should create a BankingCard with generated data and persist it")
    void shouldCreateBankingCard() {
        // given
        final Number numberMock = mock(Number.class);

        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        // when
        when(faker.number()).thenReturn(numberMock);
        when(numberMock.digits(3)).thenReturn("931");
        when(numberMock.digits(4)).thenReturn("1234");
        when(bankingCardRepository.save(any(BankingCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankingCard createdCard = bankingCardService.createCard(givenBankAccount, BankingCardType.DEBIT);

        // then
        assertThat(createdCard).isNotNull();
        assertThat(createdCard.getAssociatedBankingAccount()).isEqualTo(givenBankAccount);
        assertThat(createdCard.getCardType()).isEqualTo(BankingCardType.DEBIT);
        assertThat(createdCard.getCardPin()).isEqualTo("1234");
        assertThat(createdCard.getCardCvv()).isEqualTo("931");
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should cancel a BankingCard")
    void shouldCancelBankingCard() {
        // given
        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        // when
        when(bankingCardRepository.save(any(BankingCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankingCard cancelledCard = bankingCardService.cancelCard(givenBankingCard);

        // then
        assertThat(cancelledCard.getCardNumber()).isEqualTo(givenBankingCard.getCardNumber());
        assertThat(cancelledCard.getCardType()).isEqualTo(givenBankingCard.getCardType());
        assertThat(cancelledCard.getCardStatus()).isEqualTo(BankingCardStatus.DISABLED);
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should cancel a BankingCard when you are admin")
    void shouldCancelRequestBankingCardWhenYouAreAdmin() {
        // given
        setUpContext(customerAdmin);
        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        // password confirmation
        PasswordConfirmationRequest request = new PasswordConfirmationRequest("123456");

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
        when(bankingCardRepository.save(any(BankingCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankingCard cancelledCard = bankingCardService.cancelCardRequest(givenBankingCard.getId(), request);

        // then
        assertThat(cancelledCard.getCardNumber()).isEqualTo(givenBankingCard.getCardNumber());
        assertThat(cancelledCard.getCardType()).isEqualTo(givenBankingCard.getCardType());
        assertThat(cancelledCard.getCardStatus()).isEqualTo(BankingCardStatus.DISABLED);
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should not cancel a BankingCard when not exists")
    void shouldNotCancelRequestBankingCardWhenNotExists() {
        // given
        setUpContext(customerA);
        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        // password confirmation
        PasswordConfirmationRequest request = new PasswordConfirmationRequest("123456");

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(
                BankingCardNotFoundException.class,
                () -> bankingCardService.cancelCardRequest(givenBankingCard.getId(), request)
        );

        // then
        verify(bankingCardRepository, times(0)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should not cancel a BankingCard when its not yours")
    void shouldNotCancelRequestBankingCardWhenItsNotYours() {
        // given
        setUpContext(customerA);
        BankingAccount givenBankAccount = new BankingAccount(customerB);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        // password confirmation
        PasswordConfirmationRequest request = new PasswordConfirmationRequest("123456");

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));

        assertThrows(
                BankingCardAuthorizationException.class,
                () -> bankingCardService.cancelCardRequest(givenBankingCard.getId(), request)
        );

        // then
        verify(bankingCardRepository, times(0)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should not cancel a BankingCard when password not match")
    void shouldNotCancelRequestBankingCardWhenPasswordNotMatch() {
        // given
        setUpContext(customerA);
        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        // password confirmation
        PasswordConfirmationRequest request = new PasswordConfirmationRequest("1234567");

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));

        assertThrows(
                PasswordMismatchException.class,
                () -> bankingCardService.cancelCardRequest(givenBankingCard.getId(), request)
        );

        // then
        verify(bankingCardRepository, times(0)).save(any(BankingCard.class));
    }


    @Test
    @DisplayName("Should set PIN to BankingCard")
    void shouldSetBankingCardPin() {
        // given
        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        BankingCardSetPinRequest request = new BankingCardSetPinRequest("7777", "123456");

        // when
        when(bankingCardRepository.save(any(BankingCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankingCard savedCard = bankingCardService.setBankingCardPin(givenBankingCard, request.pin());

        // then
        assertThat(savedCard).isNotNull();
        assertThat(savedCard.getCardPin()).isEqualTo(request.pin());
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should set PIN to BankingCard")
    void shouldSetBankingCardPinRequest() {
        // given
        setUpContext(customerA);
        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        BankingCardSetPinRequest request = new BankingCardSetPinRequest("7777", "123456");

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
        when(bankingCardRepository.save(any(BankingCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankingCard savedCard = bankingCardService.setBankingCardPinRequest(givenBankingCard.getId(), request);

        // then
        assertThat(savedCard).isNotNull();
        assertThat(savedCard.getCardPin()).isEqualTo(request.pin());
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should set daily limit to BankingCard")
    void shouldSetBankingCardDailyLimit() {
        // given
        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        BankingCardSetDailyLimitRequest request = new BankingCardSetDailyLimitRequest(
                BigDecimal.valueOf(7777), "123456"
        );

        // when
        when(bankingCardRepository.save(any(BankingCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankingCard savedCard = bankingCardService.setDailyLimit(givenBankingCard, request.dailyLimit());

        // then
        assertThat(savedCard).isNotNull();
        assertThat(savedCard.getDailyLimit()).isEqualTo(request.dailyLimit());
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should set daily limit to BankingCard")
    void shouldSetBankingCardDailyLimitRequest() {
        // given
        setUpContext(customerA);
        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        BankingCardSetDailyLimitRequest request = new BankingCardSetDailyLimitRequest(
                BigDecimal.valueOf(7777), "123456"
        );

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
        when(bankingCardRepository.save(any(BankingCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankingCard savedCard = bankingCardService.setDailyLimitRequest(
                givenBankingCard.getId(), request
        );

        // then
        assertThat(savedCard).isNotNull();
        assertThat(savedCard.getDailyLimit()).isEqualTo(request.dailyLimit());
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should set lock/unlock to BankingCard")
    void shouldSetBankingCardLockStatus() {
        // given
        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardStatus(BankingCardStatus.ENABLED);
        givenBankingCard.setLockStatus(BankingCardLockStatus.UNLOCKED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        // when
        when(bankingCardRepository.save(any(BankingCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankingCard savedCard = bankingCardService.setCardLockStatus(
                givenBankingCard,
                BankingCardLockStatus.LOCKED
        );

        // then
        assertThat(savedCard).isNotNull();
        assertThat(savedCard.getLockStatus()).isEqualTo(BankingCardLockStatus.LOCKED);
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should set lock/unlock to BankingCard")
    void shouldSetBankingCardLockStatusRequest() {
        // given
        setUpContext(customerA);
        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(5L);
        givenBankAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        givenBankAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard();
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setLockStatus(BankingCardLockStatus.LOCKED);
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);

        PasswordConfirmationRequest request = new PasswordConfirmationRequest(
                "123456"
        );

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
        when(bankingCardRepository.save(any(BankingCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankingCard savedCard = bankingCardService.setCardLockStatusRequest(
                givenBankingCard.getId(), BankingCardLockStatus.UNLOCKED, request
        );

        // then
        assertThat(savedCard).isNotNull();
        assertThat(savedCard.getLockStatus()).isEqualTo(BankingCardLockStatus.UNLOCKED);
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));
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
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);
        givenBankingCard.setCardNumber("1234567890123456");
        givenBankingCard.setCardType(BankingCardType.CREDIT);
        givenBankingCard.setLockStatus(BankingCardLockStatus.LOCKED);

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
        givenBankingCard.setAssociatedBankingAccount(givenBankAccount);
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
