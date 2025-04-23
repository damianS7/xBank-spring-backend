package com.damian.xBank.auth;

import com.damian.xBank.auth.http.request.AuthenticationRequest;
import com.damian.xBank.auth.http.request.AuthenticationResponse;
import com.damian.xBank.common.utils.JWTUtil;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerGender;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class AuthorizationIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private JWTUtil jwtUtil;

    private String rawPassword = "123456";
    private Customer customer;
    private Customer admin;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        customer = new Customer();
        customer.setEmail("customer@test.com");
        customer.setPassword(bCryptPasswordEncoder.encode(rawPassword));
        customer.getProfile().setNationalId("123456789Z");
        customer.getProfile().setName("John");
        customer.getProfile().setSurname("Wick");
        customer.getProfile().setPhone("123 123 123");
        customer.getProfile().setGender(CustomerGender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
        customer.getProfile().setCountry("USA");
        customer.getProfile().setAddress("fake ave");
        customer.getProfile().setPostalCode("050012");
        customer.getProfile().setPhotoPath("no photoPath");

        customerRepository.save(customer);

        admin = new Customer();
        admin.setEmail("admin@test.com");
        admin.setPassword(bCryptPasswordEncoder.encode(rawPassword));
        admin.setRole(CustomerRole.ADMIN);

        customerRepository.save(admin);
    }

    String loginWithCustomer(Customer customer) throws Exception {
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

        return response.token();
    }

    @Test
    void shouldHaveAccessWhenAdmin() throws Exception {
        // given
        final String token = loginWithCustomer(admin);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/admin/customers/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldNotHaveAccessWhenNotAdmin() throws Exception {
        // given
        final String token = loginWithCustomer(customer);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/admin/customers/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(403))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }


}
