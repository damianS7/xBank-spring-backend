package com.damian.xBank.auth;

import com.damian.xBank.auth.http.request.AuthenticationRequest;
import com.damian.xBank.auth.http.request.AuthenticationResponse;
import com.damian.xBank.common.utils.JWTUtil;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.http.request.CustomerRegistrationRequest;
import com.damian.xBank.customer.profile.CustomerGender;
import com.damian.xBank.customer.profile.http.request.ProfileUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "JWT_SECRET_KEY=THIS-IS-A-BIG-SECRET!-KEEP-IT-SAFE")
public class AuthenticationControllerTest {
    private final String email = "john@gmail.com";
    private final String rawPassword = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private JWTUtil jwtUtil;

    private Customer customer;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        customerRepository.deleteAll();
        customer = new Customer();
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

    @Test
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
    void shouldNotLoginWhenInvalidCredentials() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest(
                this.email, "badPassword"
        );

        // request to json
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(500))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // json response to AuthenticationResponse
        AuthenticationResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthenticationResponse.class
        );

        // then
        assertThat(response.customer()).isNull();
        assertThat(response.token()).isNull();
    }

    @Test
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
    void shouldNotLoginWhenNullFields() throws Exception {
        // Given
        AuthenticationRequest request = new AuthenticationRequest(
                null, null
        );

        // request to json
        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(containsString("Validation error")))
                .andReturn();
    }

    @Test
    void shouldRegisterCustomer() throws Exception {
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
                .andExpect(MockMvcResultMatchers.status().is(500))
                .andExpect(jsonPath("$.message").value("Email is taken."))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
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
    void shouldNotHaveAccessWhenTokenHasExpired() throws Exception {
        // given
        final String expiredToken = jwtUtil.generateToken(
                customer.getEmail(),
                new Date(System.currentTimeMillis() - 1000 * 60 * 60)
        );

        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "david",
                "white",
                "123 123 123",
                LocalDate.of(1989, 1, 1),
                CustomerGender.MALE,
                "-",
                "Fake AV 51",
                "50120",
                "USA",
                "123123123Z",
                this.rawPassword
        );

        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/v1/profiles/" + customer.getProfile().getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                        .content(jsonRequest))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(401))
                .andExpect(jsonPath("$.message").value("Token expired"))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

}
