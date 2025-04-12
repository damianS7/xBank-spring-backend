package com.damian.xBank.profile;

import com.damian.xBank.auth.AuthenticationService;
import com.damian.xBank.auth.http.AuthenticationRequest;
import com.damian.xBank.auth.http.AuthenticationResponse;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.profile.http.ProfileUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "JWT_SECRET_KEY=THIS-IS-A-BIG-SECRET!-KEEP-IT-SAFE")
public class ProfileControllerTest {
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

    private Customer customer;
    private String token;

    @BeforeAll
    void setUp() throws Exception {

        customer = new Customer();
        customer.setEmail(this.email);
        customer.setPassword(bCryptPasswordEncoder.encode(this.rawPassword));
        customer.getProfile().setNationalId("123456789Z");
        customer.getProfile().setName("John");
        customer.getProfile().setSurname("Wick");
        customer.getProfile().setGender(Gender.MALE);
        customer.getProfile().setBirthdate("1/1/1989");
        customer.getProfile().setCountry("USA");
        customer.getProfile().setAddress("fake ave");
        customer.getProfile().setPostalCode("050012");
        customer.getProfile().setPhoto("no photo");

        customerRepository.save(customer);

        // given
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                customer.getEmail(), this.rawPassword
        );

        String jsonRequest = objectMapper.writeValueAsString(authenticationRequest);

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/auth/login")
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
    void shouldUpdateProfile() throws Exception {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                customer.getProfile().getId(),
                "david",
                "white",
                "123 123 123",
                "1/1/1980",
                Gender.MALE,
                "-",
                "Fake AV 51",
                "50120",
                "USA",
                "123123123Z",
                customer.getId(),
                this.rawPassword
        );

        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/v1/profile/" + request.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(jsonRequest))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        ProfileDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProfileDTO.class
        );

        // then
        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.surname()).isEqualTo(request.surname());
        assertThat(response.address()).isEqualTo(request.address());
        assertThat(response.phone()).isEqualTo(request.phone());
        assertThat(response.birthdate()).isEqualTo(request.birthdate());
        assertThat(response.gender()).isEqualTo(request.gender());
        assertThat(response.photo()).isEqualTo(request.photo());
        assertThat(response.postalCode()).isEqualTo(request.postalCode());
        assertThat(response.nationalId()).isEqualTo(request.nationalId());
    }

    @Test
    void shouldNotUpdateProfileWhenAnyFieldIsEmpty() throws Exception {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                customer.getProfile().getId(),
                "david",
                "",
                "123 123 123",
                "1/1/1980",
                Gender.MALE,
                "-",
                "Fake AV 51",
                "50120",
                "USA",
                "123123123Z",
                customer.getId(),
                this.rawPassword
        );

        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/v1/profile/" + request.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(jsonRequest))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldNotUpdateProfileWhenAnyFieldIsNull() throws Exception {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                customer.getProfile().getId(),
                "david",
                null,
                "123 123 123",
                "1/1/1980",
                Gender.MALE,
                "-",
                "Fake AV 51",
                "50120",
                "USA",
                "123123123Z",
                customer.getId(),
                this.rawPassword
        );

        String jsonRequest = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/v1/profile/" + request.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(jsonRequest))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }
}