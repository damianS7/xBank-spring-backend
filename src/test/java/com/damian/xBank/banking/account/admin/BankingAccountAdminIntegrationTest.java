package com.damian.xBank.banking.account.admin;

import com.damian.xBank.auth.http.AuthenticationRequest;
import com.damian.xBank.auth.http.AuthenticationResponse;
import com.damian.xBank.banking.account.*;
import com.damian.xBank.banking.account.http.request.BankingAccountAliasUpdateRequest;
import com.damian.xBank.banking.account.http.request.BankingAccountCloseRequest;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class BankingAccountAdminIntegrationTest {
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
    @DisplayName("Should set status of to OPEN")
    void shouldSetAccountStatusToOpen() throws Exception {
        // given
        loginWithCustomer(customerAdmin);
        BankingAccountCloseRequest request = new BankingAccountCloseRequest(
                RAW_PASSWORD
        );

        BankingAccount givenBankingAccount = new BankingAccount(customerB);
        givenBankingAccount.setAccountNumber("US0011111111222222223333");
        givenBankingAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountRepository.save(givenBankingAccount);

        // when
        MvcResult result = mockMvc
                .perform(
                        patch("/api/v1/admin/banking/accounts/{id}/open", givenBankingAccount.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        BankingAccountDTO bankingAccount = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BankingAccountDTO.class
        );

        // then
        assertThat(bankingAccount).isNotNull();
        assertThat(bankingAccount.accountStatus()).isEqualTo(BankingAccountStatus.OPEN);
    }

    @Test
    @DisplayName("Should set status of to CLOSED")
    void shouldSetAccountStatusToClose() throws Exception {
        // given
        loginWithCustomer(customerAdmin);
        BankingAccountCloseRequest request = new BankingAccountCloseRequest(
                RAW_PASSWORD
        );

        BankingAccount givenBankingAccount = new BankingAccount(customerB);
        givenBankingAccount.setAccountNumber("US0011111111222222223333");
        givenBankingAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountRepository.save(givenBankingAccount);

        // when
        MvcResult result = mockMvc
                .perform(
                        patch("/api/v1/admin/banking/accounts/{id}/close", givenBankingAccount.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        BankingAccountDTO bankingAccount = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BankingAccountDTO.class
        );

        // then
        assertThat(bankingAccount).isNotNull();
        assertThat(bankingAccount.accountStatus()).isEqualTo(BankingAccountStatus.CLOSED);
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
    @DisplayName("Should close an account even if its not yours when you are ADMIN")
    void shouldUpdateBankingAccountAlias() throws Exception {
        // given
        loginWithCustomer(customerAdmin);
        BankingAccountAliasUpdateRequest request = new BankingAccountAliasUpdateRequest(
                "New Alias",
                RAW_PASSWORD
        );

        BankingAccount givenBankingAccount = new BankingAccount(customerB);
        givenBankingAccount.setAccountNumber("US0011111111222222223333");
        givenBankingAccount.setAccountType(BankingAccountType.SAVINGS);
        givenBankingAccount.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountRepository.save(givenBankingAccount);

        // when
        MvcResult result = mockMvc
                .perform(
                        patch("/api/v1/admin/banking/accounts/{id}/alias", givenBankingAccount.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        BankingAccountDTO bankingAccountDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BankingAccountDTO.class
        );

        // then
        assertThat(bankingAccountDTO).isNotNull();
        assertThat(bankingAccountDTO.alias()).isEqualTo(request.alias());
    }
}