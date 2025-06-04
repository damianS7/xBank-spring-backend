package com.damian.xBank.banking.card.admin;

import com.damian.xBank.auth.http.AuthenticationRequest;
import com.damian.xBank.auth.http.AuthenticationResponse;
import com.damian.xBank.banking.account.*;
import com.damian.xBank.banking.card.*;
import com.damian.xBank.banking.card.http.BankingCardSetDailyLimitRequest;
import com.damian.xBank.banking.card.http.BankingCardSetLockStatusRequest;
import com.damian.xBank.banking.card.http.BankingCardSetPinRequest;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class BankingCardAdminIntegrationTest {
    private final String RAW_PASSWORD = "123456";

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

    private Customer customerA;
    private Customer customerB;
    private Customer customerAdmin;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        customerRepository.deleteAll();
        bankingAccountRepository.deleteAll();
        bankingCardRepository.deleteAll();

        customerA = new Customer();
        customerA.setEmail("customerA@test.com");
        customerA.setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD));
        customerA.getProfile().setFirstName("alice");
        customerA.getProfile().setLastName("wonderland");
        customerA.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));

        customerRepository.save(customerA);

        customerB = new Customer();
        customerB.setEmail("customerB@test.com");
        customerB.setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD));
        customerB.getProfile().setFirstName("alice");
        customerB.getProfile().setLastName("wonderland");
        customerB.getProfile().setBirthdate(LocalDate.of(1995, 11, 11));

        customerRepository.save(customerB);

        customerAdmin = new Customer();
        customerAdmin.setEmail("customerC@test.com");
        customerAdmin.setRole(CustomerRole.ADMIN);
        customerAdmin.setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD));
        customerAdmin.getProfile().setFirstName("alice");
        customerAdmin.getProfile().setLastName("wonderland");
        customerAdmin.getProfile().setBirthdate(LocalDate.of(1995, 11, 11));

        customerRepository.save(customerAdmin);
    }

    void loginWithCustomer(Customer customer) throws Exception {
        // given
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                customer.getEmail(), this.RAW_PASSWORD
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
    @DisplayName("Should get a customer banking cards")
    void shouldGetCustomerBankingCards() throws Exception {
        loginWithCustomer(customerAdmin);
        BankingAccount bankingAccount = new BankingAccount();
        bankingAccount.setOwner(customerA);
        bankingAccount.setAccountNumber("12345678");
        bankingAccount.setBalance(BigDecimal.valueOf(100));
        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setCreatedAt(Instant.now());

        BankingCard bankingCard = new BankingCard();
        bankingCard.setCardNumber("12345678");
        bankingCard.setCardPin("1234");
        bankingCard.setCardCvv("123");
        bankingAccount.addBankingCard(bankingCard);

        bankingAccountRepository.save(bankingAccount);

        MvcResult result = mockMvc.perform(get("/api/v1/admin/customers/{id}/banking/cards", customerA.getId())
                                          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                                  .andDo(print())
                                  .andExpect(status().is(200))
                                  .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                                  .andReturn();

        Set<BankingCardDTO> bankingCardDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<Set<BankingCardDTO>>() {
                }
        );

        // then
        assertThat(bankingCardDTO).isNotNull();
        assertEquals(bankingCardDTO.size(), 1);
    }

    @Test
    @DisplayName("Should cancel a banking card")
    void shouldCancelBankingCard() throws Exception {
        // given
        loginWithCustomer(customerAdmin);

        BankingAccount bankingAccount = new BankingAccount();
        bankingAccount.setOwner(customerA);
        bankingAccount.setAccountNumber("12345678");
        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);

        BankingCard bankingCard = new BankingCard();
        bankingCard.setCardNumber("12345678");
        bankingCard.setCardPin("1234");
        bankingCard.setCardCvv("123");
        bankingAccount.addBankingCard(bankingCard);

        bankingAccountRepository.save(bankingAccount);

        // when
        MvcResult result = mockMvc
                .perform(
                        patch("/api/v1/admin/banking/cards/{id}/cancel", bankingCard.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        BankingCardDTO bankingCardDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BankingCardDTO.class
        );

        // then
        assertThat(bankingCardDTO).isNotNull();
        assertEquals(bankingCardDTO.cardStatus(), BankingCardStatus.DISABLED);
    }

    @Test
    @DisplayName("Should lock a banking card")
    void shouldLockBankingCard() throws Exception {
        // given
        loginWithCustomer(customerAdmin);

        BankingCardSetLockStatusRequest request = new BankingCardSetLockStatusRequest(
                BankingCardLockStatus.LOCKED,
                "123456"
        );

        BankingAccount bankingAccount = new BankingAccount();
        bankingAccount.setOwner(customerA);
        bankingAccount.setAccountNumber("12345678");
        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);

        BankingCard bankingCard = new BankingCard();
        bankingCard.setCardNumber("12345678");
        bankingCard.setCardPin("1234");
        bankingCard.setCardCvv("123");
        bankingAccount.addBankingCard(bankingCard);

        bankingAccountRepository.save(bankingAccount);

        // when
        MvcResult result = mockMvc
                .perform(
                        patch("/api/v1/admin/banking/cards/{id}/lock-status", bankingCard.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        BankingCardDTO bankingCardDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BankingCardDTO.class
        );

        // then
        assertThat(bankingCardDTO).isNotNull();
        assertEquals(bankingCardDTO.lockStatus(), BankingCardLockStatus.LOCKED);
    }

    @Test
    @DisplayName("Should set daily limit to a banking card")
    void shouldSetDailyLimitBankingCard() throws Exception {
        // given
        loginWithCustomer(customerAdmin);
        BigDecimal dailyLimit = BigDecimal.valueOf(7777);

        BankingCardSetDailyLimitRequest request = new BankingCardSetDailyLimitRequest(
                dailyLimit,
                "123456"
        );

        BankingAccount bankingAccount = new BankingAccount();
        bankingAccount.setOwner(customerA);
        bankingAccount.setAccountNumber("12345678");
        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);

        BankingCard bankingCard = new BankingCard();
        bankingCard.setCardNumber("12345678");
        bankingCard.setCardPin("1234");
        bankingCard.setCardCvv("123");
        bankingAccount.addBankingCard(bankingCard);

        bankingAccountRepository.save(bankingAccount);

        // when
        MvcResult result = mockMvc
                .perform(
                        patch("/api/v1/admin/banking/cards/{id}/daily-limit", bankingCard.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        BankingCardDTO bankingCardDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BankingCardDTO.class
        );

        // then
        assertThat(bankingCardDTO).isNotNull();
        assertEquals(bankingCardDTO.dailyLimit(), dailyLimit);
    }

    @Test
    @DisplayName("Should set banking card pin")
    void shouldSetBankingCardPin() throws Exception {
        // given
        loginWithCustomer(customerAdmin);
        String pin = "1234";

        BankingCardSetPinRequest request = new BankingCardSetPinRequest(
                pin,
                "123456"
        );

        BankingAccount bankingAccount = new BankingAccount();
        bankingAccount.setOwner(customerA);
        bankingAccount.setAccountNumber("12345678");
        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);

        BankingCard bankingCard = new BankingCard();
        bankingCard.setCardNumber("12345678");
        bankingCard.setCardPin("1234");
        bankingCard.setCardCvv("123");
        bankingAccount.addBankingCard(bankingCard);

        bankingAccountRepository.save(bankingAccount);

        // when
        MvcResult result = mockMvc
                .perform(
                        patch("/api/v1/admin/banking/cards/{id}/pin", bankingCard.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        BankingCardDTO bankingCardDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BankingCardDTO.class
        );

        // then
        assertThat(bankingCardDTO).isNotNull();
        assertEquals(bankingCardDTO.cardPIN(), pin);
    }

}