package com.damian.xBank.banking.card;

import com.damian.xBank.auth.http.AuthenticationRequest;
import com.damian.xBank.auth.http.AuthenticationResponse;
import com.damian.xBank.auth.http.PasswordConfirmationRequest;
import com.damian.xBank.banking.account.*;
import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
import com.damian.xBank.banking.card.http.BankingCardSetDailyLimitRequest;
import com.damian.xBank.banking.card.http.BankingCardSetPinRequest;
import com.damian.xBank.banking.transactions.BankingTransactionType;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class BankingCardIntegrationTest {
    private final String rawPassword = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BankingAccountRepository bankingAccountRepository;

    @Autowired
    private BankingCardRepository bankingCardRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private BankingAccountService bankingAccountService;

    private Customer customerA;
    private Customer customerB;
    private Customer customerAdmin;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        customerRepository.deleteAll();

        customerA = new Customer();
        customerA.setEmail("customerA@test.com");
        customerA.setPassword(bCryptPasswordEncoder.encode(this.rawPassword));
        customerA.getProfile().setFirstName("alice");
        customerA.getProfile().setLastName("wonderland");
        customerA.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));

        customerRepository.save(customerA);

        customerB = new Customer();
        customerB.setEmail("customerB@test.com");
        customerB.setPassword(bCryptPasswordEncoder.encode(this.rawPassword));
        customerB.getProfile().setFirstName("alice");
        customerB.getProfile().setLastName("wonderland");
        customerB.getProfile().setBirthdate(LocalDate.of(1995, 11, 11));

        customerRepository.save(customerB);

        customerAdmin = new Customer();
        customerAdmin.setEmail("customerC@test.com");
        customerAdmin.setRole(CustomerRole.ADMIN);
        customerAdmin.setPassword(bCryptPasswordEncoder.encode(this.rawPassword));
        customerAdmin.getProfile().setFirstName("alice");
        customerAdmin.getProfile().setLastName("wonderland");
        customerAdmin.getProfile().setBirthdate(LocalDate.of(1995, 11, 11));

        customerRepository.save(customerAdmin);
    }

    void loginWithCustomer(Customer customer) throws Exception {
        // given
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                customer.getEmail(), this.rawPassword
        );

        String jsonRequest = objectMapper.writeValueAsString(authenticationRequest);

        // when
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(jsonRequest))
                                  .andReturn();

        AuthenticationResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthenticationResponse.class
        );

        token = response.token();
    }

    @Test
    @DisplayName("Should fetch customers banking cards")
    void shouldFetchBankingCards() throws Exception {
        // given
        loginWithCustomer(customerA);

        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountNumber("ES1234567890123456789012");
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount.setBalance(BigDecimal.valueOf(1000));

        BankingCard bankingCard = new BankingCard();
        bankingCard.setCardType(BankingCardType.CREDIT);
        bankingCard.setCardNumber("1234567890123456");
        bankingCard.setCardStatus(BankingCardStatus.ENABLED);
        bankingCard.setAssociatedBankingAccount(bankingAccount);

        bankingAccount.addBankingCard(bankingCard);
        bankingAccountRepository.save(bankingAccount);

        // when
        // then
        mockMvc.perform(get("/api/v1/customers/me/banking/cards")
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
               .andDo(print())
               .andExpect(status().is(200));
    }

    @Test
    @DisplayName("Should cancel a BankingCard")
    void shouldCancelBankingCard() throws Exception {
        // given
        loginWithCustomer(customerA);

        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountNumber("ES1234567890123456789012");
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount.setBalance(BigDecimal.valueOf(1000));

        BankingCard bankingCard = new BankingCard();
        bankingCard.setCardType(BankingCardType.CREDIT);
        bankingCard.setCardNumber("1234567890123456");
        bankingCard.setCardStatus(BankingCardStatus.ENABLED);
        bankingCard.setAssociatedBankingAccount(bankingAccount);

        bankingAccount.addBankingCard(bankingCard);
        bankingAccountRepository.save(bankingAccount);

        PasswordConfirmationRequest request = new PasswordConfirmationRequest("123456");

        // when
        // then
        MvcResult result = mockMvc.perform(post("/api/v1/customers/me/banking/cards/{id}/cancel", bankingCard.getId())
                                          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(objectMapper.writeValueAsString(request)))
                                  .andDo(print())
                                  .andExpect(status().is(200))
                                  .andReturn();

        BankingCardDTO card = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BankingCardDTO.class
        );

        assertThat(card).isNotNull();
        assertThat(card.cardStatus()).isEqualTo(BankingCardStatus.DISABLED);
    }

    @Test
    @DisplayName("Should set a PIN on a BankingCard")
    void shouldSetBankingCardPin() throws Exception {
        // given
        loginWithCustomer(customerA);

        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountNumber("ES1234567890123456789012");
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount.setBalance(BigDecimal.valueOf(1000));

        BankingCard bankingCard = new BankingCard();
        bankingCard.setCardType(BankingCardType.CREDIT);
        bankingCard.setCardNumber("1234567890123456");
        bankingCard.setCardStatus(BankingCardStatus.ENABLED);
        bankingCard.setAssociatedBankingAccount(bankingAccount);

        bankingAccount.addBankingCard(bankingCard);
        bankingAccountRepository.save(bankingAccount);

        BankingCardSetPinRequest request = new BankingCardSetPinRequest("7777", "123456");

        // when
        // then
        MvcResult result = mockMvc
                .perform(put("/api/v1/customers/me/banking/cards/{id}/pin", bankingCard.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        BankingCardDTO card = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BankingCardDTO.class
        );

        assertThat(card).isNotNull();
        assertThat(card.cardPIN()).isEqualTo(request.pin());
    }

    @Test
    @DisplayName("Should set a Daily Limit on a BankingCard")
    void shouldSetBankingCardDailyLimit() throws Exception {
        // given
        loginWithCustomer(customerA);

        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountNumber("ES1234567890123456789012");
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount.setBalance(BigDecimal.valueOf(1000));

        BankingCard bankingCard = new BankingCard();
        bankingCard.setCardType(BankingCardType.CREDIT);
        bankingCard.setCardNumber("1234567890123456");
        bankingCard.setCardStatus(BankingCardStatus.ENABLED);
        bankingCard.setAssociatedBankingAccount(bankingAccount);

        bankingAccount.addBankingCard(bankingCard);
        bankingAccountRepository.save(bankingAccount);

        BankingCardSetDailyLimitRequest request = new BankingCardSetDailyLimitRequest(
                BigDecimal.valueOf(7777),
                "123456"
        );

        // when
        // then
        MvcResult result = mockMvc
                .perform(
                        put("/api/v1/customers/me/banking/cards/{id}/daily-limit", bankingCard.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        BankingCardDTO card = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BankingCardDTO.class
        );

        assertThat(card).isNotNull();
        assertThat(card.dailyLimit()).isEqualTo(request.dailyLimit());
    }

    @Test
    @DisplayName("Should lock BankingCard")
    void shouldLockBankingCard() throws Exception {
        // given
        loginWithCustomer(customerA);

        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountNumber("ES1234567890123456789012");
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount.setBalance(BigDecimal.valueOf(1000));

        BankingCard bankingCard = new BankingCard();
        bankingCard.setCardType(BankingCardType.CREDIT);
        bankingCard.setCardNumber("1234567890123456");
        bankingCard.setCardStatus(BankingCardStatus.ENABLED);
        bankingCard.setAssociatedBankingAccount(bankingAccount);

        bankingAccount.addBankingCard(bankingCard);
        bankingAccountRepository.save(bankingAccount);

        BankingCardSetDailyLimitRequest request = new BankingCardSetDailyLimitRequest(
                BigDecimal.valueOf(7777),
                "123456"
        );

        // when
        // then
        MvcResult result = mockMvc
                .perform(
                        put("/api/v1/customers/me/banking/cards/{id}/lock", bankingCard.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        BankingCardDTO card = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BankingCardDTO.class
        );

        assertThat(card).isNotNull();
        assertThat(card.lockStatus()).isEqualTo(BankingCardLockStatus.LOCKED);
    }

    @Test
    @DisplayName("Should unlock BankingCard")
    void shouldUnlockBankingCard() throws Exception {
        // given
        loginWithCustomer(customerA);

        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountNumber("ES1234567890123456789012");
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount.setBalance(BigDecimal.valueOf(1000));

        BankingCard bankingCard = new BankingCard();
        bankingCard.setCardType(BankingCardType.CREDIT);
        bankingCard.setCardNumber("1234567890123456");
        bankingCard.setCardStatus(BankingCardStatus.ENABLED);
        bankingCard.setAssociatedBankingAccount(bankingAccount);

        bankingAccount.addBankingCard(bankingCard);
        bankingAccountRepository.save(bankingAccount);

        BankingCardSetDailyLimitRequest request = new BankingCardSetDailyLimitRequest(
                BigDecimal.valueOf(7777),
                "123456"
        );

        // when
        // then
        MvcResult result = mockMvc
                .perform(
                        put("/api/v1/customers/me/banking/cards/{id}/unlock", bankingCard.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        BankingCardDTO card = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BankingCardDTO.class
        );

        assertThat(card).isNotNull();
        assertThat(card.lockStatus()).isEqualTo(BankingCardLockStatus.UNLOCKED);
    }

    @Test
    @Disabled
    @DisplayName("Should create a transaction card charge")
    void shouldCreateTransactionCardCharge() throws Exception {
        // given
        loginWithCustomer(customerA);

        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountNumber("ES1234567890123456789012");
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount.setBalance(BigDecimal.valueOf(1000));

        BankingCard bankingCard = new BankingCard();
        bankingCard.setCardType(BankingCardType.CREDIT);
        bankingCard.setCardNumber("1234567890123456");
        bankingCard.setCardStatus(BankingCardStatus.ENABLED);
        bankingCard.setAssociatedBankingAccount(bankingAccount);

        bankingAccount.addBankingCard(bankingCard);
        bankingAccountRepository.save(bankingAccount);

        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
                null,
                BigDecimal.valueOf(100),
                BankingTransactionType.CARD_CHARGE,
                "Amazon.com"
        );

        // when
        // then
        mockMvc.perform(post("/api/v1/customers/me/banking/cards/" + bankingCard.getId() + "/spend")
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andDo(print())
               .andExpect(status().is(201));
    }
}