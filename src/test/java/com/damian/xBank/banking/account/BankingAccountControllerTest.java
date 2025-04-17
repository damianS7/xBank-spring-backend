package com.damian.xBank.banking.account;

import com.damian.xBank.auth.http.request.AuthenticationRequest;
import com.damian.xBank.auth.http.request.AuthenticationResponse;
import com.damian.xBank.banking.account.http.request.BankingAccountOpenRequest;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "JWT_SECRET_KEY=THIS-IS-A-BIG-SECRET!-KEEP-IT-SAFE")
public class BankingAccountControllerTest {
    private final String email = "john@gmail.com";
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

    @MockitoBean
    private BankingAccountService bankingAccountService;

    private Customer customer;
    private String token;

    @BeforeAll
    void setUp() throws Exception {

        customer = new Customer();
        customer.setEmail(this.email);
        customer.setPassword(bCryptPasswordEncoder.encode(this.rawPassword));
        customer.getProfile().setNationalId("123456789Z");
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));

        customerRepository.save(customer);

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
    void shouldOpenBankingAccount() throws Exception {
        // given
        BankingAccountOpenRequest request = new BankingAccountOpenRequest(
                BankingAccountType.SAVINGS,
                BankingAccountCurrency.EUR
        );

        BankingAccount response = new BankingAccount();
        response.setId(1L);
        response.setAccountNumber("ES1234567890123456789012");
        response.setAccountType(BankingAccountType.SAVINGS);
        response.setAccountCurrency(BankingAccountCurrency.EUR);
        response.setAccountStatus(BankingAccountStatus.OPEN);
        response.setBalance(BigDecimal.ZERO);

        // when
        when(bankingAccountService.openBankingAccount(request)).thenReturn(response);

        // then
        mockMvc.perform(post("/api/v1/banking_account/open")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("ES1234567890123456789012"))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.accountCurrency").value("EUR"))
                .andExpect(jsonPath("$.accountStatus").value("OPEN"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void shouldGetCustomerWithBankingAccount() throws Exception {
        Set<BankingAccount> bankingAccounts = new HashSet<>();

        BankingAccount bankingAccount1 = new BankingAccount();
        bankingAccount1.setCustomer(customer);
        bankingAccount1.setAccountNumber("12345678");
        bankingAccount1.setBalance(BigDecimal.valueOf(100));
        bankingAccount1.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount1.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount1.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount1.setCreatedAt(Instant.now());
        bankingAccounts.add(bankingAccount1);

        BankingAccount bankingAccount2 = new BankingAccount();
        bankingAccount2.setCustomer(customer);
        bankingAccount2.setAccountNumber("001231443");
        bankingAccount2.setBalance(BigDecimal.valueOf(350));
        bankingAccount2.setAccountStatus(BankingAccountStatus.OPEN);
        bankingAccount2.setAccountType(BankingAccountType.SAVINGS);
        bankingAccount2.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccount2.setCreatedAt(Instant.now());
        bankingAccounts.add(bankingAccount2);

        customer.setBankingAccounts(bankingAccounts);

        bankingAccountRepository.save(bankingAccount1);
        bankingAccountRepository.save(bankingAccount2);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customer/" + customer.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankingAccounts.[?(@.id == " + bankingAccount1.getId() + ")].accountNumber").value(bankingAccount1.getAccountNumber()))
                .andExpect(jsonPath("$.bankingAccounts.[?(@.id == " + bankingAccount2.getId() + ")].accountNumber").value(bankingAccount2.getAccountNumber()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }
}