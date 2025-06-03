package com.damian.xBank.banking.transaction;

import com.damian.xBank.auth.http.AuthenticationRequest;
import com.damian.xBank.auth.http.AuthenticationResponse;
import com.damian.xBank.banking.account.*;
import com.damian.xBank.banking.transactions.BankingTransaction;
import com.damian.xBank.banking.transactions.BankingTransactionDTO;
import com.damian.xBank.banking.transactions.BankingTransactionType;
import com.damian.xBank.banking.transactions.http.BankingAccountTransactionRequest;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class BankingTransactionAccountIntegrationTest {
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
    @DisplayName("Should create a transaction account deposit")
    void shouldCreateTransactionAccountDeposit() throws Exception {
        // given
        loginWithCustomer(customerA);

        BigDecimal givenDepositAmount = BigDecimal.valueOf(100);

        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountNumber("ES1234567890123456789012");
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount.setBalance(BigDecimal.valueOf(1000));
        bankingAccountRepository.save(bankingAccount);

        BankingAccountTransactionRequest request = new BankingAccountTransactionRequest(
                null,
                BankingTransactionType.DEPOSIT,
                "Deposit",
                BigDecimal.valueOf(100),
                RAW_PASSWORD
        );

        // when
        MvcResult result = mockMvc
                .perform(post("/api/v1/customers/me/banking/accounts/{id}/transactions", bankingAccount.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(201))
                .andReturn();

        // then
        BankingTransactionDTO transaction = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BankingTransactionDTO.class
        );

        BankingAccount updatedBankingAccount = bankingAccountRepository.findById(bankingAccount.getId()).get();

        // then
        assertThat(transaction).isNotNull();
        assertEquals(
                updatedBankingAccount.getBalance(),
                bankingAccount.getBalance().add(givenDepositAmount).setScale(2)
        );
        assertEquals(transaction.transactionType(), BankingTransactionType.DEPOSIT);
        assertEquals(transaction.amount(), givenDepositAmount);
    }

    @Test
    @DisplayName("Should transfer to another customer")
    void shouldTransferToAnotherCustomer() throws Exception {
        // given
        loginWithCustomer(customerA);

        BigDecimal givenTransferAmount = BigDecimal.valueOf(100);

        BankingAccount bankingAccountA = new BankingAccount(customerA);
        bankingAccountA.setAccountNumber("ES1234567890123456789012");
        bankingAccountA.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountA.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountA.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccountA.setBalance(BigDecimal.valueOf(3200));
        bankingAccountRepository.save(bankingAccountA);

        BankingAccount bankingAccountB = new BankingAccount(customerB);
        bankingAccountB.setAccountNumber("DE1234567890123456789012");
        bankingAccountB.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountB.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountB.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccountB.setBalance(BigDecimal.valueOf(200));
        bankingAccountRepository.save(bankingAccountB);

        BankingAccountTransactionRequest request = new BankingAccountTransactionRequest(
                bankingAccountB.getAccountNumber(),
                BankingTransactionType.TRANSFER_TO,
                "Enjoy!",
                givenTransferAmount,
                RAW_PASSWORD
        );

        // when
        MvcResult result = mockMvc
                .perform(post(
                        "/api/v1/customers/me/banking/accounts/" + bankingAccountA.getId() + "/transactions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(201))
                .andReturn();

        BankingTransactionDTO transaction = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BankingTransactionDTO.class
        );

        BankingAccount updatedBankingAccountA = bankingAccountRepository.findById(bankingAccountA.getId()).get();

        // then
        assertThat(transaction).isNotNull();
        assertEquals(
                updatedBankingAccountA.getBalance(),
                bankingAccountA.getBalance().subtract(givenTransferAmount).setScale(2)
        );
        assertEquals(transaction.transactionType(), BankingTransactionType.TRANSFER_TO);
        assertEquals(transaction.amount(), givenTransferAmount);
        assertEquals(transaction.description(), "Enjoy!");
        assertEquals(transaction.bankingAccountId(), bankingAccountA.getId());
    }

    @Test
    @DisplayName("Should not transfer to same account number")
    void shouldNotTransferToSameAccount() throws Exception {
        // given
        loginWithCustomer(customerA);

        BigDecimal givenTransferAmount = BigDecimal.valueOf(100);

        BankingAccount bankingAccountA = new BankingAccount(customerA);
        bankingAccountA.setAccountNumber("ES1234567890123456789012");
        bankingAccountA.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountA.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountA.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccountA.setBalance(BigDecimal.valueOf(3200));
        bankingAccountRepository.save(bankingAccountA);

        BankingAccount bankingAccountB = new BankingAccount(customerB);
        bankingAccountB.setAccountNumber("DE1234567890123456789012");
        bankingAccountB.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountB.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountB.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccountB.setBalance(BigDecimal.valueOf(200));
        bankingAccountRepository.save(bankingAccountB);

        BankingAccountTransactionRequest request = new BankingAccountTransactionRequest(
                bankingAccountA.getAccountNumber(),
                BankingTransactionType.TRANSFER_TO,
                "Enjoy!",
                givenTransferAmount,
                RAW_PASSWORD
        );

        // when
        MvcResult result = mockMvc
                .perform(post(
                        "/api/v1/customers/me/banking/accounts/" + bankingAccountA.getId() + "/transactions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(403))
                .andReturn();

        // then
    }

    @Test
    @DisplayName("Should not transfer when account is not closed")
    void shouldNotTransferWhenAccountIsNotClosed() throws Exception {
        // given
        loginWithCustomer(customerA);
        BigDecimal givenTransferAmount = BigDecimal.valueOf(100);

        BankingAccount bankingAccountA = new BankingAccount(customerA);
        bankingAccountA.setAccountNumber("ES1234567890123456789012");
        bankingAccountA.setAccountStatus(BankingAccountStatus.CLOSED);
        bankingAccountA.setBalance(BigDecimal.valueOf(3200));
        bankingAccountRepository.save(bankingAccountA);

        BankingAccount bankingAccountB = new BankingAccount(customerB);
        bankingAccountB.setAccountNumber("DE1234567890123456789012");
        bankingAccountB.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccountB.setBalance(BigDecimal.valueOf(200));
        bankingAccountRepository.save(bankingAccountB);

        BankingAccountTransactionRequest request = new BankingAccountTransactionRequest(
                bankingAccountB.getAccountNumber(),
                BankingTransactionType.TRANSFER_TO,
                "Enjoy!",
                givenTransferAmount,
                RAW_PASSWORD
        );


        BankingTransaction transaction = new BankingTransaction(bankingAccountA);
        transaction.setTransactionType(request.transactionType());
        transaction.setDescription(request.description());
        transaction.setAmount(request.amount());

        // when
        mockMvc.perform(post("/api/v1/customers/me/banking/accounts/" + bankingAccountA.getId() + "/transactions")
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andDo(print())
               .andExpect(status().is(403));

        // then
    }

    @Test
    @DisplayName("Should not transfer to when insufficient funds")
    void shouldTransferToWhenInsufficientFunds() throws Exception {
        // given
        loginWithCustomer(customerA);

        BigDecimal givenTransferAmount = BigDecimal.valueOf(100);

        BankingAccount bankingAccountA = new BankingAccount(customerA);
        bankingAccountA.setAccountNumber("ES1234567890123456789012");
        bankingAccountA.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountA.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountA.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccountA.setBalance(BigDecimal.valueOf(0));
        bankingAccountRepository.save(bankingAccountA);

        BankingAccount bankingAccountB = new BankingAccount(customerB);
        bankingAccountB.setAccountNumber("DE1234567890123456789012");
        bankingAccountB.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountB.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountB.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccountB.setBalance(BigDecimal.valueOf(200));
        bankingAccountRepository.save(bankingAccountB);

        BankingAccountTransactionRequest request = new BankingAccountTransactionRequest(
                bankingAccountB.getAccountNumber(),
                BankingTransactionType.TRANSFER_TO,
                "Enjoy!",
                givenTransferAmount,
                RAW_PASSWORD
        );

        // when
        MvcResult result = mockMvc
                .perform(post(
                        "/api/v1/customers/me/banking/accounts/" + bankingAccountA.getId() + "/transactions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(403))
                .andReturn();
    }
}