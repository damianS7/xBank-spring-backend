package com.damian.xBank.auth;

import com.damian.xBank.auth.http.AuthenticationRequest;
import com.damian.xBank.auth.http.AuthenticationResponse;
import com.damian.xBank.common.utils.JWTUtil;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerGender;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import com.damian.xBank.customer.http.request.CustomerPasswordUpdateRequest;
import com.damian.xBank.customer.http.request.CustomerRegistrationRequest;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationIntegrationTest {
    private final String email = "customer@test.com";
    private final String rawPassword = "123456";

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

    private Customer customer;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        customer = new Customer();
        customer.setRole(CustomerRole.ADMIN);
        customer.setEmail(this.email);
        customer.setPassword(bCryptPasswordEncoder.encode(this.rawPassword));
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
    @DisplayName("Should login when valid credentials")
    void shouldLoginWhenValidCredentials() throws Exception {
        // given
        AuthenticationRequest request = new AuthenticationRequest(
                this.email, this.rawPassword
        );

        // request to json
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                                          .post("/api/v1/auth/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(jsonRequest))
                                  .andDo(print())
                                  .andExpect(MockMvcResultMatchers.status().is(200))
                                  .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                                  .andReturn();

        // json to AuthenticationResponse
        AuthenticationResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthenticationResponse.class
        );

        // then
        final String emailFromToken = jwtUtil.extractEmail(response.token());
        assertThat(response.customer().email()).isEqualTo(this.email);
        assertThat(emailFromToken).isEqualTo(this.email);
    }

    @Test
    @DisplayName("Should not login when invalid credentials")
    void shouldNotLoginWhenInvalidCredentials() throws Exception {
        // given
        AuthenticationRequest request = new AuthenticationRequest(
                this.email, "badPassword"
        );

        // request to json
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(jsonRequest))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(401))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not login when account is disabled")
    void shouldNotLoginWhenAccountIsDisabled() throws Exception {
        // given
        Customer givenCustomer = new Customer();
        givenCustomer.setEmail("disabled-customer@test.com");
        givenCustomer.setPassword(bCryptPasswordEncoder.encode(this.rawPassword));
        givenCustomer.getAuth().setAuthAccountStatus(AuthAccountStatus.DISABLED);

        customerRepository.save(givenCustomer);

        AuthenticationRequest request = new AuthenticationRequest(
                givenCustomer.getEmail(), "123456"
        );

        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(jsonRequest))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(401))
               .andExpect(jsonPath("$.message").value("Account is disabled."))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not login when invalid email format")
    void shouldNotLoginWhenInvalidEmailFormat() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest(
                "thisIsNotAnEmail", "123456"
        );

        // request to json
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(jsonRequest))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.errors.email").value(containsString("must be a well-formed email address")))
               .andExpect(jsonPath("$.message").value("Validation error"));
    }

    @Test
    @DisplayName("Should not login when null fields")
    void shouldNotLoginWhenNullFields() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest(
                null, null
        );

        // request to json
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(jsonRequest))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.message").value(containsString("Validation error")));
    }

    @Test
    @DisplayName("Should register customer when request is valid")
    void shouldRegisterCustomerWhenValidRequest() throws Exception {
        // given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "david@gmail.com",
                "12345678X$",
                "david",
                "white",
                "123 123 123",
                LocalDate.of(1989, 1, 1),
                CustomerGender.MALE,
                "",
                "Fake AV",
                "50120",
                "USA",
                "123123123Z"
        );

        // request to json
        String json = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/auth/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(json))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(201))
               .andExpect(jsonPath("$.email").value(request.email()))
               .andExpect(jsonPath("$.profile.name").value(request.name()))
               .andExpect(jsonPath("$.profile.surname").value(request.surname()))
               .andExpect(jsonPath("$.profile.phone").value(request.phone()))
               .andExpect(jsonPath("$.profile.birthdate").value(request.birthdate().toString()))
               .andExpect(jsonPath("$.profile.gender").value(request.gender().toString()))
               .andExpect(jsonPath("$.profile.address").value(request.address()))
               .andExpect(jsonPath("$.profile.postalCode").value(request.postalCode()))
               .andExpect(jsonPath("$.profile.country").value(request.country()))
               .andExpect(jsonPath("$.profile.nationalId").value(request.nationalId()))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not register customer when missing fields")
    void shouldNotRegisterCustomerWhenMissingFields() throws Exception {
        // given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "david@test.com",
                "123456",
                "david",
                "white",
                "123 123 123",
                null,
                CustomerGender.MALE,
                "",
                "",
                null,
                "USA",
                "123123123Z"
        );

        // request to json
        String json = objectMapper.writeValueAsString(request);

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(json))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(jsonPath("$.message").value(containsString("Validation error")))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not register customer when email is not well-formed")
    void shouldNotRegisterCustomerWhenEmailIsNotWellFormed() throws Exception {
        // given
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "badEmail",
                "1234567899X$",
                "david",
                "white",
                "123 123 123",
                LocalDate.of(1989, 1, 1),
                CustomerGender.MALE,
                "",
                "fake ave",
                "55555",
                "USA",
                "123123123Z"
        );

        // request to json
        String json = objectMapper.writeValueAsString(request);

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(json))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(jsonPath("$.message").value("Validation error"))
               .andExpect(jsonPath("$.errors.email").value(containsString("Email must be a well-formed email address")))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not register customer when email is taken")
    void shouldNotRegisterCustomerWhenEmailIsTaken() throws Exception {
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                this.email,
                "12345678X$",
                "david",
                "white",
                "123 123 123",
                LocalDate.of(1989, 1, 1),
                CustomerGender.MALE,
                "",
                "Fake AV",
                "50120",
                "USA",
                "123123123Z"
        );

        // request to json
        String json = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/auth/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(json))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(409))
               .andExpect(jsonPath("$.message").value(containsString("is already taken.")))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should not register customer when password policy not satisfied")
    void shouldNotRegisterCustomerWhenPasswordPolicyNotSatisfied() throws Exception {
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                this.email,
                "123456",
                "david",
                "white",
                "123 123 123",
                LocalDate.of(1989, 1, 1),
                CustomerGender.MALE,
                "",
                "Fake AV",
                "50120",
                "USA",
                "123123123Z"
        );

        // request to json
        String json = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                       .post("/api/v1/auth/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(json))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(jsonPath("$.message").value("Validation error"))
               .andExpect(jsonPath("$.errors.password").value(containsString("Password must be at least")))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should update password")
    void shouldUpdatePassword() throws Exception {
        // given
        String token = loginWithCustomer(customer);
        CustomerPasswordUpdateRequest updatePasswordRequest = new CustomerPasswordUpdateRequest(
                "123456",
                "12345678$Xa"
        );

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/auth/customers/password")
                                              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(updatePasswordRequest)))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("Should update password")
    void shouldNotUpdatePasswordWhenPasswordMismatch() throws Exception {
        // given
        String token = loginWithCustomer(customer);
        CustomerPasswordUpdateRequest updatePasswordRequest = new CustomerPasswordUpdateRequest(
                "1234564",
                "12345678$Xa"
        );

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/auth/customers/password")
                                              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(updatePasswordRequest)))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(403));
    }

    @Test
    @DisplayName("Should update password")
    void shouldNotUpdatePasswordWhenPasswordPolicyNotSatisfied() throws Exception {
        // given
        String token = loginWithCustomer(customer);
        CustomerPasswordUpdateRequest updatePasswordRequest = new CustomerPasswordUpdateRequest(
                "1234564",
                "1234"
        );

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/auth/customers/password")
                                              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(updatePasswordRequest)))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(jsonPath("$.message").value("Validation error"))
               .andExpect(jsonPath("$.errors.newPassword").value(containsString("Password must be at least")))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should update password")
    void shouldNotUpdatePasswordWhenPasswordIsNull() throws Exception {
        // given
        String token = loginWithCustomer(customer);
        CustomerPasswordUpdateRequest updatePasswordRequest = new CustomerPasswordUpdateRequest(
                "1234564",
                null
        );

        // when
        // then
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/auth/customers/password")
                                              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(objectMapper.writeValueAsString(updatePasswordRequest)))
               .andDo(print())
               .andExpect(MockMvcResultMatchers.status().is(400))
               .andExpect(jsonPath("$.message").value("Validation error"))
               .andExpect(jsonPath("$.errors.newPassword").value(containsString("must not be blank")))
               .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }
}
