package com.damian.xBank.banking.account;

import com.damian.xBank.auth.http.AuthenticationRequest;
import com.damian.xBank.auth.http.AuthenticationResponse;
import com.damian.xBank.banking.account.exception.BankingAccountException;
import com.damian.xBank.banking.account.http.request.BankingAccountOpenRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountTransactionCreateRequest;
import com.damian.xBank.banking.transactions.BankingTransaction;
import com.damian.xBank.banking.transactions.BankingTransactionType;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class BankingAccountIntegrationTest {
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
    @DisplayName("Should open a banking account")
    void shouldOpenBankingAccount() throws Exception {
        // given
        loginWithCustomer(customerA);
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
               .andExpect(status().is(201))
               .andExpect(jsonPath("$.accountNumber").isNotEmpty())
               .andExpect(jsonPath("$.accountType").value("SAVINGS"))
               .andExpect(jsonPath("$.accountCurrency").value("EUR"))
               .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    @DisplayName("Should close your own banking account")
    void shouldCloseBankingAccount() throws Exception {
        // given
        loginWithCustomer(customerA);
        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountNumber("US00 1111 1111 2222 2222 3333");
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountRepository.save(bankingAccount);

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/banking/accounts/" + bankingAccount.getId() + "/close")
                                              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
               .andDo(print())
               .andExpect(status().is(200));
    }

    @Test
    @DisplayName("Should not close account if you are not the owner and you are not admin either")
    void shouldNotCloseBankingAccountWhenItsNotYoursAndYouAreNotAdmin() throws Exception {
        // given
        loginWithCustomer(customerA);
        BankingAccount bankingAccount = new BankingAccount(customerB);
        bankingAccount.setAccountNumber("US00 1111 1111 2222 2222 3333");
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountRepository.save(bankingAccount);

        // when
        // then
        mockMvc.perform(get("/api/v1/banking/accounts/" + bankingAccount.getId() + "/close")
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
               .andDo(print())
               .andExpect(status().is(403));
    }

    @Test
    @DisplayName("Should close an account even if its not yours when you are ADMIN")
    void shouldCloseBankingAccountWhenItsNotYoursAndButYouAreAdmin() throws Exception {
        // given
        loginWithCustomer(customerAdmin);
        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountNumber("US00 1111 1111 2222 2222 3333");
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountRepository.save(bankingAccount);

        // when
        // then
        mockMvc.perform(get("/api/v1/banking/accounts/" + bankingAccount.getId() + "/close")
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
               .andDo(print())
               .andExpect(status().is(200));
    }

    @Test
    @DisplayName("Should get a customer with its banking account data")
    void shouldGetCustomerWithBankingAccount() throws Exception {
        loginWithCustomer(customerAdmin);
        Set<BankingAccount> bankingAccounts = new HashSet<>();

        BankingAccount bankingAccount1 = new BankingAccount();
        bankingAccount1.setOwner(customerAdmin);
        bankingAccount1.setAccountNumber("12345678");
        bankingAccount1.setBalance(BigDecimal.valueOf(100));
        bankingAccount1.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount1.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount1.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount1.setCreatedAt(Instant.now());
        bankingAccounts.add(bankingAccount1);

        BankingAccount bankingAccount2 = new BankingAccount();
        bankingAccount2.setOwner(customerAdmin);
        bankingAccount2.setAccountNumber("001231443");
        bankingAccount2.setBalance(BigDecimal.valueOf(350));
        bankingAccount2.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount2.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount2.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount2.setCreatedAt(Instant.now());
        bankingAccounts.add(bankingAccount2);

        customerAdmin.setBankingAccounts(bankingAccounts);

        bankingAccountRepository.save(bankingAccount1);
        bankingAccountRepository.save(bankingAccount2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/customers/" + customerAdmin.getId())
                                              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
               .andDo(print())
               .andExpect(status().is(200))
               .andExpect(jsonPath(
                       "$.bankingAccounts.[?(@.id == " + bankingAccount1.getId() + ")].accountNumber").value(
                       bankingAccount1.getAccountNumber()))
               .andExpect(jsonPath(
                       "$.bankingAccounts.[?(@.id == " + bankingAccount2.getId() + ")].accountNumber").value(
                       bankingAccount2.getAccountNumber()))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should create a transaction deposit")
    void shouldCreateTransactionDeposit() throws Exception {
        // given
        loginWithCustomer(customerA);
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
                BankingTransactionType.DEPOSIT,
                "Enjoy!"
        );

        BankingTransaction transaction = new BankingTransaction(bankingAccount);
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
               .andExpect(status().is(201));
    }

    @Test
    @DisplayName("Should create a transfer transaction")
    void shouldTransferToAnotherCustomer() throws Exception {
        // given
        loginWithCustomer(customerA);
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
                BankingTransactionType.TRANSFER_TO,
                "Enjoy!"
        );

        // when
        // then
        mockMvc.perform(post("/api/v1/banking/accounts/" + bankingAccountA.getId() + "/transactions")
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andDo(print())
               .andExpect(status().is(201));
    }

    @Test
    @DisplayName("Should not create a transaction when account is not open")
    void shouldNotCreateTransactionWhenAccountIsNotOpen() throws Exception {
        // given
        loginWithCustomer(customerA);
        BankingAccount bankingAccount = new BankingAccount(customerA);
        bankingAccount.setAccountNumber("ES1234567890123456789012");
        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount.setAccountStatus(BankingAccountStatus.CLOSED);
        bankingAccount.setBalance(BigDecimal.ZERO);

        bankingAccountRepository.save(bankingAccount);

        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
                null,
                BigDecimal.valueOf(1000),
                BankingTransactionType.DEPOSIT,
                "Enjoy!"
        );

        BankingTransaction transaction = new BankingTransaction(bankingAccount);
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

    @Test
    @DisplayName("Should not transfer to same banking account")
    void shouldNotTransferToSameBankingAccount() throws Exception {
        // given
        loginWithCustomer(customerA);
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
                BankingTransactionType.TRANSFER_TO,
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
    @DisplayName("Should not create transaction when insufficient funds")
    void shouldNotCreateTransactionWhenInsufficientFunds() throws Exception {
        // given
        loginWithCustomer(customerA);
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
                BankingTransactionType.CARD_CHARGE,
                "Enjoy!"
        );

        BankingTransaction transaction = new BankingTransaction(bankingAccount);
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
               .andExpect(status().is(409));
    }

    @Test
    @DisplayName("Should rollback transfer if receiver account does not exist")
    void shouldRollbackTransferIfReceiverAccountDoesNotExist() {
        // given
        Customer customerA = new Customer("customerA@test.com", "123456");
        Customer customerB = new Customer("customerB@test.com", "123456");

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                customerA, null, Collections.emptyList()));

        BankingAccount senderAccount = new BankingAccount(customerA);
        senderAccount.setAccountNumber("US00 0000 1111 2222 3333 4444");
        senderAccount.setAccountType(BankingAccountType.SAVINGS);
        senderAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        senderAccount.setBalance(BigDecimal.valueOf(1000));
        customerRepository.save(customerA);

        BankingAccount receiverAccount = new BankingAccount(customerB);
        receiverAccount.setAccountNumber("US00 0000 1111 2222 3333 5555");
        receiverAccount.setAccountType(BankingAccountType.SAVINGS);
        receiverAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        receiverAccount.setBalance(BigDecimal.valueOf(1000));
        customerRepository.save(customerB);

        // when
        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
                999L, // ID que no existe para forzar el fallo
                BigDecimal.valueOf(200),
                BankingTransactionType.TRANSFER_TO,
                "Test Transfer"
        );

        assertThrows(
                BankingAccountException.class,
                () -> bankingAccountService.handleCreateTransactionRequest(senderAccount.getId(), request)
        );

        // then
        BankingAccount refreshedSenderAccount = bankingAccountRepository.findById(senderAccount.getId()).get();
        BankingAccount refreshedReceiverAccount = bankingAccountRepository.findById(receiverAccount.getId()).get();

        // Verificamos que el balance NO haya cambiado (rollback)
        assertThat(refreshedSenderAccount.getBalance()).isEqualTo(BigDecimal.valueOf(1000).setScale(3));
        assertThat(refreshedReceiverAccount.getBalance()).isEqualTo(BigDecimal.valueOf(1000).setScale(3));

        // Verificamos que no se haya guardado ninguna transacción
        assertThat(refreshedSenderAccount.getAccountTransactions()).isEmpty();
        assertThat(refreshedReceiverAccount.getAccountTransactions()).isEmpty();
    }
}