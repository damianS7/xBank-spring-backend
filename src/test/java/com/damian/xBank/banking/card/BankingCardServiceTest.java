package com.damian.xBank.banking.card;

import com.damian.xBank.auth.http.PasswordConfirmationRequest;
import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountCurrency;
import com.damian.xBank.banking.account.BankingAccountType;
import com.damian.xBank.banking.card.exception.BankingCardAuthorizationException;
import com.damian.xBank.banking.card.exception.BankingCardNotFoundException;
import com.damian.xBank.banking.card.http.BankingCardSetDailyLimitRequest;
import com.damian.xBank.banking.card.http.BankingCardSetLockStatusRequest;
import com.damian.xBank.banking.card.http.BankingCardSetPinRequest;
import com.damian.xBank.common.exception.PasswordMismatchException;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import net.datafaker.Faker;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankingCardServiceTest {

    @Mock
    private BankingCardRepository bankingCardRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private Faker faker;

    @InjectMocks
    private BankingCardService bankingCardService;

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

        BankingCard createdCard = bankingCardService.createBankingCard(givenBankAccount, BankingCardType.DEBIT);

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
        setUpContext(customerA);
        PasswordConfirmationRequest givenRequest = new PasswordConfirmationRequest(RAW_PASSWORD);

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
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
        when(bankingCardRepository.save(any(BankingCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankingCard cancelledCard = bankingCardService.cancelCard(givenBankingCard.getId(), givenRequest);

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
        PasswordConfirmationRequest request = new PasswordConfirmationRequest(RAW_PASSWORD);

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
        when(bankingCardRepository.save(any(BankingCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankingCard cancelledCard = bankingCardService.cancelCard(givenBankingCard.getId(), request);

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
        PasswordConfirmationRequest request = new PasswordConfirmationRequest(RAW_PASSWORD);

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(
                BankingCardNotFoundException.class,
                () -> bankingCardService.cancelCard(givenBankingCard.getId(), request)
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
        PasswordConfirmationRequest request = new PasswordConfirmationRequest(RAW_PASSWORD);

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));

        assertThrows(
                BankingCardAuthorizationException.class,
                () -> bankingCardService.cancelCard(givenBankingCard.getId(), request)
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
                () -> bankingCardService.cancelCard(givenBankingCard.getId(), request)
        );

        // then
        verify(bankingCardRepository, times(0)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should set PIN to BankingCard")
    void shouldSetBankingCardPin() {
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

        BankingCardSetPinRequest request = new BankingCardSetPinRequest("7777", RAW_PASSWORD);

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
        when(bankingCardRepository.save(any(BankingCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankingCard savedCard = bankingCardService.setBankingCardPin(givenBankingCard.getId(), request);

        // then
        assertThat(savedCard).isNotNull();
        assertThat(savedCard.getCardPin()).isEqualTo(request.pin());
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should set daily limit to BankingCard")
    void shouldSetBankingCardDailyLimit() {
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
                BigDecimal.valueOf(7777),
                RAW_PASSWORD
        );

        // when
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
        when(bankingCardRepository.save(any(BankingCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankingCard savedCard = bankingCardService.setDailyLimit(givenBankingCard.getId(), request);

        // then
        assertThat(savedCard).isNotNull();
        assertThat(savedCard.getDailyLimit()).isEqualTo(request.dailyLimit());
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));
    }

    @Test
    @DisplayName("Should set lock/unlock to BankingCard")
    void shouldSetBankingCardLockStatus() {
        // given
        setUpContext(customerA);
        BankingCardSetLockStatusRequest givenRequest = new BankingCardSetLockStatusRequest(
                BankingCardLockStatus.LOCKED,
                RAW_PASSWORD
        );

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
        when(bankingCardRepository.findById(anyLong())).thenReturn(Optional.of(givenBankingCard));
        when(bankingCardRepository.save(any(BankingCard.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BankingCard savedCard = bankingCardService.setCardLockStatus(
                givenBankingCard.getId(),
                givenRequest
        );

        // then
        assertThat(savedCard).isNotNull();
        assertThat(savedCard.getLockStatus()).isEqualTo(givenRequest.lockStatus());
        verify(bankingCardRepository, times(1)).save(any(BankingCard.class));
    }
}
