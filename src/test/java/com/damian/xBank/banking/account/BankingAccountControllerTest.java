package com.damian.xBank.banking.account;

import com.damian.xBank.auth.http.request.AuthenticationRequest;
import com.damian.xBank.auth.http.request.AuthenticationResponse;
import com.damian.xBank.banking.account.http.request.BankingAccountOpenRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
import com.damian.xBank.banking.account.transactions.BankingAccountTransaction;
import com.damian.xBank.banking.account.transactions.BankingAccountTransactionType;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankingAccountControllerTest {
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
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    //    @MockitoBean
    private BankingAccountService bankingAccountService;

    private Customer customerA;
    private Customer customerB;
    private String token;

    @BeforeAll
    void setUp() throws Exception {
        customerRepository.deleteAll();

        customerA = new Customer();
        customerA.setEmail("customerA@test.com");
        customerA.setPassword(bCryptPasswordEncoder.encode(this.rawPassword));
        customerA.getProfile().setName("alice");
        customerA.getProfile().setSurname("wonderland");
        customerA.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));

        customerRepository.save(customerA);

        customerB = new Customer();
        customerB.setEmail("customerB@test.com");
        customerB.setPassword(bCryptPasswordEncoder.encode(this.rawPassword));
        customerB.getProfile().setName("alice");
        customerB.getProfile().setSurname("wonderland");
        customerB.getProfile().setBirthdate(LocalDate.of(1995, 11, 11));

        customerRepository.save(customerB);

        // given
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                customerA.getEmail(), this.rawPassword
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
    void shouldOpenBankingAccount() throws Exception {
        // given
        BankingAccountOpenRequest request = new BankingAccountOpenRequest(
                BankingAccountType.SAVINGS,
                BankingAccountCurrency.EUR
        );

        // when
        // then
        mockMvc.perform(post("/api/v1/banking/accounts/open")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").isNotEmpty())
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.accountCurrency").value("EUR"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void shouldGetCustomerWithBankingAccount() throws Exception {
        Set<BankingAccount> bankingAccounts = new HashSet<>();

        BankingAccount bankingAccount1 = new BankingAccount();
        bankingAccount1.setCustomer(customerA);
        bankingAccount1.setAccountNumber("12345678");
        bankingAccount1.setBalance(BigDecimal.valueOf(100));
        bankingAccount1.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount1.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount1.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount1.setCreatedAt(Instant.now());
        bankingAccounts.add(bankingAccount1);

        BankingAccount bankingAccount2 = new BankingAccount();
        bankingAccount2.setCustomer(customerA);
        bankingAccount2.setAccountNumber("001231443");
        bankingAccount2.setBalance(BigDecimal.valueOf(350));
        bankingAccount2.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount2.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount2.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount2.setCreatedAt(Instant.now());
        bankingAccounts.add(bankingAccount2);

        customerA.setBankingAccounts(bankingAccounts);

        bankingAccountRepository.save(bankingAccount1);
        bankingAccountRepository.save(bankingAccount2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customers/" + customerA.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankingAccounts.[?(@.id == " + bankingAccount1.getId() + ")].accountNumber").value(bankingAccount1.getAccountNumber()))
                .andExpect(jsonPath("$.bankingAccounts.[?(@.id == " + bankingAccount2.getId() + ")].accountNumber").value(bankingAccount2.getAccountNumber()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldCreateTransactionDeposit() throws Exception {
        // given
        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountNumber("ES1234567890123456789012");
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount.setBalance(BigDecimal.ZERO);

        bankingAccountRepository.save(bankingAccount);

        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
                null,
                BigDecimal.valueOf(1000),
                BankingAccountTransactionType.DEPOSIT,
                "Enjoy!"
        );

        BankingAccountTransaction transaction = new BankingAccountTransaction(bankingAccount);
        transaction.setTransactionType(request.transactionType());
        transaction.setDescription(request.description());
        transaction.setAmount(request.amount());

        // when
//        when(bankingAccountService.createTransaction(request)).thenReturn(transaction);

        // then
        mockMvc.perform(post("/api/v1/banking/accounts/" + bankingAccount.getId() + "/transactions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void shouldTransferToAnotherCustomer() throws Exception {
        // given
        BankingAccount bankingAccountA = new BankingAccount(customerA);
        bankingAccountA.setAccountNumber("ES12 3456 7890 1234 5678 9012");
        bankingAccountA.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountA.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountA.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccountA.setBalance(BigDecimal.valueOf(3200));

        bankingAccountRepository.save(bankingAccountA);

        BankingAccount bankingAccountB = new BankingAccount(customerB);
        bankingAccountB.setAccountNumber("DE12 3456 7890 1234 5678 9012");
        bankingAccountB.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountB.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountB.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccountB.setBalance(BigDecimal.valueOf(200));

        bankingAccountRepository.save(bankingAccountB);

        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
                bankingAccountB.getId(),
                BigDecimal.valueOf(1000),
                BankingAccountTransactionType.TRANSFER_TO,
                "Enjoy!"
        );

        // when
        // then
        mockMvc.perform(post("/api/v1/banking/accounts/" + bankingAccountA.getId() + "/transactions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void shouldNotTransferToSameAccount() throws Exception {
        // given
        BankingAccount bankingAccountA = new BankingAccount(customerA);
        bankingAccountA.setAccountNumber("ES12 3456 7890 1234 5678 9012");
        bankingAccountA.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountA.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountA.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccountA.setBalance(BigDecimal.valueOf(3200));

        bankingAccountRepository.save(bankingAccountA);

        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
                bankingAccountA.getId(),
                BigDecimal.valueOf(1000),
                BankingAccountTransactionType.TRANSFER_TO,
                "Enjoy!"
        );

        // when
        // then
        mockMvc.perform(post("/api/v1/banking/accounts/" + bankingAccountA.getId() + "/transactions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(500));
    }

    @Test
    void shouldFailToCreateTransactionSpendWhenInsufficentFunds() throws Exception {
        // given
        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountNumber("ES1234567890123456789012");
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount.setBalance(BigDecimal.ZERO);

        bankingAccountRepository.save(bankingAccount);

        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
                null,
                BigDecimal.valueOf(1000),
                BankingAccountTransactionType.CARD_CHARGE,
                "Enjoy!"
        );

        BankingAccountTransaction transaction = new BankingAccountTransaction(bankingAccount);
        transaction.setTransactionType(request.transactionType());
        transaction.setDescription(request.description());
        transaction.setAmount(request.amount());

        // when
        // then
        mockMvc.perform(post("/api/v1/banking/accounts/" + bankingAccount.getId() + "/transactions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(500));
    }
}