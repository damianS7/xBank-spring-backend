package com.damian.xBank.customer.profile;

import com.damian.xBank.auth.AuthenticationService;
import com.damian.xBank.auth.http.request.AuthenticationRequest;
import com.damian.xBank.auth.http.request.AuthenticationResponse;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerGender;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.CustomerRole;
import com.damian.xBank.customer.profile.http.request.ProfilePatchRequest;
import com.damian.xBank.customer.profile.http.request.ProfileUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
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
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class ProfileIntegrationTest {
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

    private Customer customerA;
    private Customer customerB;
    private Customer customerAdmin;

    @BeforeEach
    void setUp() throws Exception {
        customerRepository.deleteAll();

        customerA = new Customer();
        customerA.setEmail("customerA@test.com");
        customerA.setRole(CustomerRole.CUSTOMER);
        customerA.setPassword(bCryptPasswordEncoder.encode(this.rawPassword));
        customerA.getProfile().setNationalId("123456789Z");
        customerA.getProfile().setName("John");
        customerA.getProfile().setSurname("Wick");
        customerA.getProfile().setGender(CustomerGender.MALE);
        customerA.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
        customerA.getProfile().setCountry("USA");
        customerA.getProfile().setAddress("fake ave");
        customerA.getProfile().setPostalCode("050012");
        customerA.getProfile().setPhotoPath("no photoPath");
        customerRepository.save(customerA);

        customerB = new Customer();
        customerB.setPassword(bCryptPasswordEncoder.encode("123456"));
        customerB.setEmail("customerB@test.com");
        customerRepository.save(customerB);

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
    @DisplayName("Should update own profile")
    void shouldUpdateOwnProfile() throws Exception {
        // given
        final String token = loginWithCustomer(customerA);

        Map<String, Object> fields = new HashMap<>();
        fields.put("name", "alice");
        fields.put("surname", "white");
        fields.put("phone", "999 999 999");
        fields.put("birthdate", "1983-03-13");
        fields.put("gender", "FEMALE");

        ProfilePatchRequest patchProfileRequest = new ProfilePatchRequest(
                this.rawPassword,
                fields
        );

        String jsonRequest = objectMapper.writeValueAsString(patchProfileRequest);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/api/v1/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(jsonRequest))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(jsonPath("$.name").value(fields.get("name")))
                .andExpect(jsonPath("$.surname").value(fields.get("surname")))
                .andExpect(jsonPath("$.phone").value(fields.get("phone")))
                .andExpect(jsonPath("$.birthdate").value(fields.get("birthdate")))
                .andExpect(jsonPath("$.gender").value(fields.get("gender")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should update any profile when logged as admin")
    void shouldUpdateAnyProfileWhenLoggedAsAdmin() throws Exception {
        // given
        final String token = loginWithCustomer(customerAdmin);

        Map<String, Object> fields = new HashMap<>();
        fields.put("name", "alice");
        fields.put("surname", "white");
        fields.put("phone", "999 999 999");
        fields.put("birthdate", "1983-03-13");
        fields.put("gender", "FEMALE");

        ProfilePatchRequest patchProfileRequest = new ProfilePatchRequest(
                this.rawPassword,
                fields
        );

        String jsonRequest = objectMapper.writeValueAsString(patchProfileRequest);

        // when
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/api/v1/admin/profiles/" + customerA.getProfile().getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(jsonRequest))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(jsonPath("$.name").value(fields.get("name")))
                .andExpect(jsonPath("$.surname").value(fields.get("surname")))
                .andExpect(jsonPath("$.phone").value(fields.get("phone")))
                .andExpect(jsonPath("$.birthdate").value(fields.get("birthdate")))
                .andExpect(jsonPath("$.gender").value(fields.get("gender")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldNotUpdateProfileWhenAnyFieldIsEmpty() throws Exception {
        // given
        final String token = loginWithCustomer(customerAdmin);
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "david",
                "",
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
                        .put("/api/v1/admin/profiles/" + customerA.getProfile().getId())
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
        final String token = loginWithCustomer(customerAdmin);

        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "david",
                null,
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
                        .put("/api/v1/admin/profiles/" + customerA.getProfile().getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content(jsonRequest))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }
}