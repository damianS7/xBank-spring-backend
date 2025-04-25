package com.damian.xBank.customer;

import com.damian.xBank.auth.http.request.AuthenticationRequest;
import com.damian.xBank.auth.http.request.AuthenticationResponse;
import com.damian.xBank.banking.account.BankingAccount;
import com.damian.xBank.banking.account.BankingAccountCurrency;
import com.damian.xBank.banking.account.BankingAccountType;
import com.damian.xBank.customer.http.request.CustomerEmailUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CustomerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private Customer customer;
    private Customer customerAdmin;

    @BeforeAll
    void setUp() throws Exception {
        customerRepository.deleteAll();

        customer = new Customer();
        customer.setRole(CustomerRole.CUSTOMER);
        customer.setEmail("customer@test.com");
        customer.setPassword(bCryptPasswordEncoder.encode("123456"));

        customer.getProfile().setNationalId("123456789Z");
        customer.getProfile().setName("John");
        customer.getProfile().setSurname("Wick");
        customer.getProfile().setGender(CustomerGender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
        customer.getProfile().setCountry("USA");
        customer.getProfile().setAddress("fake ave");
        customer.getProfile().setPostalCode("050012");
        customer.getProfile().setPhotoPath("no photoPath");

        Set<BankingAccount> bankingAccounts = new HashSet<>();
        BankingAccount bankingAccountA = new BankingAccount(customer);
        bankingAccountA.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountA.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountA.setAccountNumber("US99 0000 1111 1122 3333 4444");
        bankingAccounts.add(bankingAccountA);

        BankingAccount bankingAccountB = new BankingAccount(customer);
        bankingAccountB.setAccountCurrency(BankingAccountCurrency.EUR);
        bankingAccountB.setAccountType(BankingAccountType.SAVINGS);
        bankingAccountB.setAccountNumber("US99 0000 1111 1122 3333 6666");
        bankingAccounts.add(bankingAccountB);
        customer.setBankingAccounts(bankingAccounts);
        customerRepository.save(customer);

        customerAdmin = new Customer();
        customerAdmin.setPassword(bCryptPasswordEncoder.encode("123456"));
        customerAdmin.setEmail("admin@test.com");
        customerAdmin.setRole(CustomerRole.ADMIN);
        customerRepository.save(customerAdmin);
    }

    String loginWithCustomer(Customer customer) throws Exception {
        // given
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                customer.getEmail(), "123456"
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

        return response.token();
    }

    @Test
    @DisplayName("Should get customer")
    void shouldGetCustomer() throws Exception {
        // given
        final String token = loginWithCustomer(customerAdmin);

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/customers/" + customer.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.email").value(customer.getEmail()))
                .andExpect(jsonPath("$.profile.address").value(customer.getProfile().getAddress()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should get customer accounts")
    void shouldGetCustomerAccounts() throws Exception {
        // given
        final String token = loginWithCustomer(customerAdmin);
        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/customers/" + customer.getId() + "/banking/accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should delete customer")
    void shouldDeleteCustomer() throws Exception {
        // given
        final String token = loginWithCustomer(customerAdmin);

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/admin/customers/" + customer.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("Should update email")
    void shouldUpdateEmail() throws Exception {
        // given
        final String token = loginWithCustomer(customer);

        CustomerEmailUpdateRequest customerEmailUpdateRequest = new CustomerEmailUpdateRequest(
                "123456",
                "customer2@test.com"
        );

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/customers/email")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerEmailUpdateRequest)))
                .andDo(print())
                .andExpect(jsonPath("$.email").value("customer2@test.com"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
