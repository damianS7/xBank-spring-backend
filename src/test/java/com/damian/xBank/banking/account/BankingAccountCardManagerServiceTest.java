package com.damian.xBank.banking.account;

import com.damian.xBank.banking.account.exception.BankingAccountAuthorizationException;
import com.damian.xBank.banking.account.exception.BankingAccountNotFoundException;
import com.damian.xBank.banking.card.BankingCard;
import com.damian.xBank.banking.card.BankingCardRepository;
import com.damian.xBank.banking.card.BankingCardService;
import com.damian.xBank.banking.card.BankingCardType;
import com.damian.xBank.banking.card.exception.BankingCardMaximumCardsPerAccountLimitReached;
import com.damian.xBank.banking.card.http.BankingCardRequest;
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

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BankingAccountCardManagerServiceTest {

    @Mock
    private BankingAccountRepository bankingAccountRepository;

    @Mock
    private BankingCardRepository bankingCardRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private BankingAccountCardManagerService bankingAccountCardManagerService;

    @Mock
    private BankingCardService bankingCardService;

    private Customer customerA;
    private Customer customerB;
    private Customer customerAdmin;

    private final String rawPassword = "123456";

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        customerA = new Customer(99L, "customerA@test.com", bCryptPasswordEncoder.encode(rawPassword));
        customerB = new Customer(92L, "customerB@test.com", bCryptPasswordEncoder.encode(rawPassword));
        customerAdmin = new Customer(95L, "admin@test.com", bCryptPasswordEncoder.encode(rawPassword));
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
    @DisplayName("Should request a BankingCard")
    void shouldRequestBankingCard() {
        // given
        setUpContext(customerA);

        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(1L);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard(givenBankAccount);
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");

        BankingCardRequest request = new BankingCardRequest(BankingCardType.CREDIT);

        // when
        when(bankingAccountRepository.findById(anyLong())).thenReturn(Optional.of(givenBankAccount));
        when(bankingCardService.createBankingCard(any(BankingAccount.class), any(BankingCardType.class)))
                .thenReturn(givenBankingCard);

        BankingCard requestedBankingCard = bankingAccountCardManagerService.requestBankingCard(
                givenBankAccount.getId(),
                request
        );

        // then
        assertThat(requestedBankingCard).isNotNull();
        assertThat(requestedBankingCard.getCardNumber()).isEqualTo(givenBankingCard.getCardNumber());
        assertThat(requestedBankingCard.getCardType()).isEqualTo(givenBankingCard.getCardType());
    }

    @Test
    @DisplayName("Should fail to request a BankingCard when account not found")
    void shouldFailToRequestBankingCardWhenAccountNotFound() {
        // given
        setUpContext(customerA);

        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(1L);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCardRequest request = new BankingCardRequest(BankingCardType.CREDIT);

        // when
        when(bankingAccountRepository.findById(anyLong())).thenReturn(Optional.empty());

        BankingAccountNotFoundException exception = assertThrows(
                BankingAccountNotFoundException.class,
                () -> bankingAccountCardManagerService.requestBankingCard(
                        givenBankAccount.getId(),
                        request
                )
        );

        // then
        assertEquals(Exceptions.ACCOUNT.NOT_FOUND, exception.getMessage());
    }

    @Test
    @DisplayName("Should fail to generate a BankingCard when BankingAccount is not yours")
    void shouldFailToGenerateBankingCardWhenBankingAccountIsNotYours() {
        // given
        setUpContext(customerA);

        BankingAccount givenBankAccount = new BankingAccount(customerB);
        givenBankAccount.setId(1L);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCardRequest request = new BankingCardRequest(BankingCardType.CREDIT);

        // when
        when(bankingAccountRepository.findById(anyLong())).thenReturn(Optional.of(givenBankAccount));

        BankingAccountAuthorizationException exception = assertThrows(
                BankingAccountAuthorizationException.class,
                () -> bankingAccountCardManagerService.requestBankingCard(
                        givenBankAccount.getId(),
                        request
                )
        );

        // then
        assertEquals(Exceptions.ACCOUNT.ACCESS_FORBIDDEN, exception.getMessage());
    }

    @Test
    @DisplayName("Should request a BankingCard when account is not yours but you are admin")
    void shouldRequestBankingCardWhenAccountIsNotYoursButYouAreAdmin() {
        // given
        setUpContext(customerAdmin);

        BankingAccount givenBankAccount = new BankingAccount(customerB);
        givenBankAccount.setId(1L);
        givenBankAccount.setAccountNumber("US9900001111112233334444");

        BankingCard givenBankingCard = new BankingCard(givenBankAccount);
        givenBankingCard.setId(11L);
        givenBankingCard.setCardNumber("1234567890123456");

        BankingCardRequest request = new BankingCardRequest(BankingCardType.CREDIT);

        // when
        when(bankingAccountRepository.findById(anyLong())).thenReturn(Optional.of(givenBankAccount));
        when(bankingCardService.createBankingCard(any(BankingAccount.class), any(BankingCardType.class)))
                .thenReturn(givenBankingCard);

        BankingCard requestedBankingCard = bankingAccountCardManagerService.requestBankingCard(
                givenBankAccount.getId(),
                request
        );

        // then
        assertThat(requestedBankingCard).isNotNull();
        assertThat(requestedBankingCard.getCardNumber()).isEqualTo(givenBankingCard.getCardNumber());
        assertThat(requestedBankingCard.getCardType()).isEqualTo(givenBankingCard.getCardType());
    }

    @Test
    @DisplayName("Should fail to request a BankingCard when reached limit")
    void shouldFailToRequestBankingCardWhenLimitReached() {
        // given
        setUpContext(customerA);

        BankingAccount givenBankAccount = new BankingAccount(customerA);
        givenBankAccount.setId(1L);
        givenBankAccount.setAccountNumber("US9900001111112233334444");
        givenBankAccount.addBankingCard(new BankingCard());
        givenBankAccount.addBankingCard(new BankingCard());
        givenBankAccount.addBankingCard(new BankingCard());
        givenBankAccount.addBankingCard(new BankingCard());
        givenBankAccount.addBankingCard(new BankingCard());

        BankingCardRequest request = new BankingCardRequest(BankingCardType.CREDIT);

        // when
        when(bankingAccountRepository.findById(anyLong())).thenReturn(Optional.of(givenBankAccount));

        BankingCardMaximumCardsPerAccountLimitReached exception = assertThrows(
                BankingCardMaximumCardsPerAccountLimitReached.class,
                () -> bankingAccountCardManagerService.requestBankingCard(
                        givenBankAccount.getId(),
                        request
                )
        );

        // then
        assertTrue(exception.getMessage().contains("The account has reached the maximum number of cards allowed"));
    }
}
