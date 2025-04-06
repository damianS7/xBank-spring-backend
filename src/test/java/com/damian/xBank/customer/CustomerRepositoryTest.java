package com.damian.xBank.customer;

import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class CustomerRepositoryTest {

    // No es necesario la inyección de dependencias a
    // travès del constructor cuando se usa DataJpaTest
    @Autowired
    private CustomerRepository customerRepository;

    private Faker faker;


    @BeforeEach
    void setUp() {
        faker = new Faker();
        customerRepository.deleteAll();
    }

    @Test
    void shouldFindCustomer() {
        String email = faker.internet().emailAddress();

        // Given
        Customer customer = new Customer(null, email, "123456");
        Customer savedCustomer = customerRepository.save(customer);

        // When
        Optional<Customer> storedCustomer = customerRepository.findById(savedCustomer.getId());

        // Then
        assertThat(storedCustomer.isEmpty()).isFalse();
        assertThat(storedCustomer.get().getId()).isNotNull();
        assertThat(storedCustomer.get().getEmail()).isEqualTo(email);
        assertThat(storedCustomer.get().getPassword()).isEqualTo("123456");
        assertThat(storedCustomer.get().getRole()).isEqualTo(CustomerRole.CUSTOMER);
    }

    @Test
    void shouldNotFindCustomer() {
        // Given
        Long customerId = -1L;

        // When
        boolean customerExists = customerRepository.existsById(customerId);

        // Then
        assertThat(customerExists).isFalse();
    }

    @Test
    void shouldSaveCustomer() {
        // Given
        Customer customer = new Customer();
        customer.setPassword("123456");
        customer.setRole(CustomerRole.CUSTOMER);
        customer.setEmail("alice@gmail.com");

        // When
        Customer savedCustomer = customerRepository.save(customer);

        // Then
        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getEmail()).isEqualTo("alice@gmail.com");
        assertThat(savedCustomer.getPassword()).isEqualTo("123456");
        assertThat(savedCustomer.getRole()).isEqualTo(CustomerRole.CUSTOMER);
    }

    @Test
    void shouldUpdateCustomer() {
        // Given
        Customer customer = new Customer();
        customer.setPassword("123456");
        customer.setRole(CustomerRole.CUSTOMER);
        customer.setEmail("alice@gmail.com");
        customerRepository.save(customer);

        // When
        customer.setEmail("alice@outlook.com");
        customer.setPassword("654321");
        customerRepository.save(customer);

        Customer storedCustomer = customerRepository.findByEmail(customer.getEmail()).get();

        // Then
        assertThat(storedCustomer.getId()).isNotNull();
        assertThat(storedCustomer.getEmail()).isEqualTo("alice@outlook.com");
        assertThat(storedCustomer.getPassword()).isEqualTo("654321");
        assertThat(storedCustomer.getRole()).isEqualTo(CustomerRole.CUSTOMER);
    }

    @Test
    void shouldDeleteCustomerByIdCustomer() {
        // Given
        Customer customer = new Customer();
        customer.setPassword("123456");
        customer.setRole(CustomerRole.CUSTOMER);
        customer.setEmail("alice@gmail.com");
        Customer savedCustomer = customerRepository.save(customer);

        // When
        customerRepository.deleteById(savedCustomer.getId());

        // Then
        assertThat(customerRepository.existsById(savedCustomer.getId())).isFalse();
    }

}
