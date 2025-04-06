package com.damian.xBank.customer;

import com.damian.xBank.customer.http.request.CustomerRegistrationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
    }

    @Test
    void canGetAllCustomers() throws Exception {
        List<CustomerDTO> customerDTOList = new ArrayList<CustomerDTO>();
        // Given
        for (int i = 0; i < 10; i++) {
            customerDTOList.add(new Customer(
                    Long.valueOf(i),
                    faker.internet().emailAddress(),
                    faker.internet().password())
                    .toDTO()
            );
        }

        // when
        when(customerService.getCustomers()).thenReturn(customerDTOList);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customers"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void canGetCustomer() throws Exception {
        Customer customer = new Customer(4L, "alice@gmail.com", "1345");
        CustomerDTO uDTO = customer.toDTO();

        // when
        when(customerService.getCustomer(customer.getId())).thenReturn(customer);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customer/" + customer.getId()))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.data.email").value(customer.getEmail()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void canCreateCustomer() throws Exception {
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "alice@gmail.com",
                "12345"
        );
        String json = objectMapper.writeValueAsString(request);

        Customer customer = new Customer(4L, request.email(), request.password());
        // when
        when(customerService.createCustomer(request)).thenReturn(customer);

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(201))
                .andExpect(jsonPath("$.data.email").value(request.email()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }
}
