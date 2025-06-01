package com.damian.xBank.banking.transaction;

import com.damian.xBank.auth.http.AuthenticationRequest;
import com.damian.xBank.auth.http.AuthenticationResponse;
import com.damian.xBank.banking.account.*;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class BankingTransactionAccountIntegrationTest {
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
    @DisplayName("Should create a transaction account deposit")
    void shouldCreateTransactionAccountDeposit() throws Exception {
        // given
        loginWithCustomer(customerA);

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
                rawPassword
        );

        // when
        // then
        mockMvc.perform(post("/api/v1/customers/me/banking/accounts/{id}/transactions", bankingAccount.getId())
                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andDo(print())
               .andExpect(status().is(201));
    }


    // TODO need more tests
    //    @Test
    //    @DisplayName("Should create a transfer transaction")
    //    void shouldTransferToAnotherCustomer() throws Exception {
    //        // given
    //        loginWithCustomer(customerA);
    //        BankingAccount bankingAccountA = new BankingAccount(customerA);
    //        bankingAccountA.setAccountNumber("ES1234567890123456789012");
    //        bankingAccountA.setAccountType(BankingAccountType.SAVINGS);
    //        bankingAccountA.setAccountCurrency(BankingAccountCurrency.EUR);
    //        bankingAccountA.setAccountStatus(BankingAccountStatus.OPEN);
    //        bankingAccountA.setBalance(BigDecimal.valueOf(3200));
    //
    //        bankingAccountRepository.save(bankingAccountA);
    //
    //        BankingAccount bankingAccountB = new BankingAccount(customerB);
    //        bankingAccountB.setAccountNumber("DE1234567890123456789012");
    //        bankingAccountB.setAccountType(BankingAccountType.SAVINGS);
    //        bankingAccountB.setAccountCurrency(BankingAccountCurrency.EUR);
    //        bankingAccountB.setAccountStatus(BankingAccountStatus.OPEN);
    //        bankingAccountB.setBalance(BigDecimal.valueOf(200));
    //
    //        bankingAccountRepository.save(bankingAccountB);
    //
    //        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
    //                bankingAccountB.getAccountNumber(),
    //                BigDecimal.valueOf(1000),
    //                BankingTransactionType.TRANSFER_TO,
    //                "Enjoy!"
    //        );
    //
    //        // when
    //        // then
    //        mockMvc.perform(post("/api/v1/customers/me/banking/account/" + bankingAccountA.getId() + "/transaction")
    //                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
    //                       .contentType(MediaType.APPLICATION_JSON)
    //                       .content(objectMapper.writeValueAsString(request)))
    //               .andDo(print())
    //               .andExpect(status().is(201));
    //    }

    //    @Test
    //    @DisplayName("Should not create a transaction when account is not open")
    //    void shouldNotCreateTransactionWhenAccountIsNotOpen() throws Exception {
    //        // given
    //        loginWithCustomer(customerA);
    //        BankingAccount bankingAccount = new BankingAccount(customerA);
    //        bankingAccount.setAccountNumber("ES1234567890123456789012");
    //        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
    //        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
    //        bankingAccount.setAccountStatus(BankingAccountStatus.CLOSED);
    //        bankingAccount.setBalance(BigDecimal.ZERO);
    //
    //        bankingAccountRepository.save(bankingAccount);
    //
    //        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
    //                null,
    //                BigDecimal.valueOf(1000),
    //                BankingTransactionType.DEPOSIT,
    //                "Enjoy!"
    //        );
    //
    //        BankingTransaction transaction = new BankingTransaction(bankingAccount);
    //        transaction.setTransactionType(request.transactionType());
    //        transaction.setDescription(request.description());
    //        transaction.setAmount(request.amount());
    //
    //        // when
    //        // then
    //        mockMvc.perform(post("/api/v1/customers/me/banking/account/" + bankingAccount.getId() + "/transaction")
    //                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
    //                       .contentType(MediaType.APPLICATION_JSON)
    //                       .content(objectMapper.writeValueAsString(request)))
    //               .andDo(print())
    //               .andExpect(status().is(500));
    //    }
    //
    //    @Test
    //    @DisplayName("Should not transfer to same banking account")
    //    void shouldNotTransferToSameBankingAccount() throws Exception {
    //        // given
    //        loginWithCustomer(customerA);
    //        BankingAccount bankingAccountA = new BankingAccount(customerA);
    //        bankingAccountA.setAccountNumber("ES12 3456 7890 1234 5678 9012");
    //        bankingAccountA.setAccountType(BankingAccountType.SAVINGS);
    //        bankingAccountA.setAccountCurrency(BankingAccountCurrency.EUR);
    //        bankingAccountA.setAccountStatus(BankingAccountStatus.OPEN);
    //        bankingAccountA.setBalance(BigDecimal.valueOf(3200));
    //
    //        bankingAccountRepository.save(bankingAccountA);
    //
    //        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
    //                bankingAccountA.getAccountNumber(),
    //                BigDecimal.valueOf(1000),
    //                BankingTransactionType.TRANSFER_TO,
    //                "Enjoy!"
    //        );
    //
    //        // when
    //        // then
    //        mockMvc.perform(post("/api/v1/banking/customers/me/account/" + bankingAccountA.getId() + "/transaction")
    //                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
    //                       .contentType(MediaType.APPLICATION_JSON)
    //                       .content(objectMapper.writeValueAsString(request)))
    //               .andDo(print())
    //               .andExpect(status().is(500));
    //    }

    //    @Test
    //    @DisplayName("Should not create transaction when insufficient funds")
    //    void shouldNotCreateTransactionWhenInsufficientFunds() throws Exception {
    //        // given
    //        loginWithCustomer(customerA);
    //        BankingAccount bankingAccount = new BankingAccount(customerA);
    //        bankingAccount.setAccountNumber("ES1234567890123456789012");
    //        bankingAccount.setAccountType(BankingAccountType.SAVINGS);
    //        bankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
    //        bankingAccount.setAccountStatus(BankingAccountStatus.OPEN);
    //        bankingAccount.setBalance(BigDecimal.ZERO);
    //
    //        bankingAccountRepository.save(bankingAccount);
    //
    //        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
    //                null,
    //                BigDecimal.valueOf(1000),
    //                BankingTransactionType.CARD_CHARGE,
    //                "Enjoy!"
    //        );
    //
    //        BankingTransaction transaction = new BankingTransaction(bankingAccount);
    //        transaction.setTransactionType(request.transactionType());
    //        transaction.setDescription(request.description());
    //        transaction.setAmount(request.amount());
    //
    //        // when
    //        // then
    //        mockMvc.perform(post("/api/v1/customers/me/banking/account/" + bankingAccount.getId() + "/transaction")
    //                       .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
    //                       .contentType(MediaType.APPLICATION_JSON)
    //                       .content(objectMapper.writeValueAsString(request)))
    //               .andDo(print())
    //               .andExpect(status().is(409));
    //    }

    //    @Test
    //    @DisplayName("Should rollback transfer if receiver account does not exist")
    //    void shouldRollbackTransferIfReceiverAccountDoesNotExist() {
    //        // given
    //        Customer customerA = new Customer("customerA@test.com", "123456");
    //        Customer customerB = new Customer("customerB@test.com", "123456");
    //
    //        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
    //                customerA, null, Collections.emptyList()));
    //
    //        BankingAccount senderAccount = new BankingAccount(customerA);
    //        senderAccount.setAccountNumber("US00 0000 1111 2222 3333 4444");
    //        senderAccount.setAccountType(BankingAccountType.SAVINGS);
    //        senderAccount.setAccountCurrency(BankingAccountCurrency.EUR);
    //        senderAccount.setBalance(BigDecimal.valueOf(1000));
    //        customerRepository.save(customerA);
    //
    //        BankingAccount receiverAccount = new BankingAccount(customerB);
    //        receiverAccount.setAccountNumber("US00 0000 1111 2222 3333 5555");
    //        receiverAccount.setAccountType(BankingAccountType.SAVINGS);
    //        receiverAccount.setAccountCurrency(BankingAccountCurrency.EUR);
    //        receiverAccount.setBalance(BigDecimal.valueOf(1000));
    //        customerRepository.save(customerB);
    //
    //        // when
    //        BankingAccountTransactionCreateRequest request = new BankingAccountTransactionCreateRequest(
    //                "NON EXISTING ACCOUNT", // ID que no existe para forzar el fallo
    //                BigDecimal.valueOf(200),
    //                BankingTransactionType.TRANSFER_TO,
    //                "Test Transfer"
    //        );
    //
    //        assertThrows(
    //                BankingAccountException.class,
    //                () -> bankingAccountService.handleCreateTransactionRequest(senderAccount.getId(), request)
    //        );
    //
    //        // then
    //        BankingAccount refreshedSenderAccount = bankingAccountRepository.findById(senderAccount.getId()).get();
    //        BankingAccount refreshedReceiverAccount = bankingAccountRepository.findById(receiverAccount.getId()).get();
    //
    //        // Verificamos que el balance NO haya cambiado (rollback)
    //        assertThat(refreshedSenderAccount.getBalance()).isEqualTo(BigDecimal.valueOf(1000).setScale(2));
    //        assertThat(refreshedReceiverAccount.getBalance()).isEqualTo(BigDecimal.valueOf(1000).setScale(2));
    //
    //        // Verificamos que no se haya guardado ninguna transacción
    //        assertThat(refreshedSenderAccount.getAccountTransactions()).isEmpty();
    //        assertThat(refreshedReceiverAccount.getAccountTransactions()).isEmpty();
    //    }

}