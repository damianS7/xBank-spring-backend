package com.damian.xBank.customer;

import com.damian.xBank.profile.Gender;
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
        // Given
        String email = faker.internet().emailAddress();
        Customer customer = new Customer(null, email, "123456");

        // When
        customerRepository.save(customer);
        Optional<Customer> optionalCustomer = customerRepository.findById(customer.getId());
        Customer storedCustomer = optionalCustomer.get();

        // Then
        assertThat(optionalCustomer).isPresent();
        assertThat(storedCustomer.getId()).isNotNull();
        assertThat(storedCustomer.getEmail()).isEqualTo(email);
        assertThat(storedCustomer.getPassword()).isEqualTo("123456");
        assertThat(storedCustomer.getRole()).isEqualTo(CustomerRole.CUSTOMER);
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
    void shouldSaveCustomerAndProfile() {
        // Given
        Customer customer = new Customer();
        customer.setEmail("david@gmail.com");
        customer.setPassword("123456");
        customer.getProfile().setNationalId("123456789Z");
        customer.getProfile().setName("david");
        customer.getProfile().setSurname("white");
        customer.getProfile().setPhone("123 123 123");
        customer.getProfile().setGender(Gender.MALE);
        customer.getProfile().setBirthdate("1/1/1980");
        customer.getProfile().setCountry("USA");
        customer.getProfile().setAddress("fake av");
        customer.getProfile().setPostalCode("501200");
        customer.getProfile().setPhoto("/images/photo.jpg");

        // When
        customerRepository.save(customer);

        // Then
        assertThat(customer.getId()).isNotNull();
    }

    @Test
    void shouldUpdateCustomer() {
        // Given
        String oldEmail = "alice@gmail.com";
        String newEmail = "alice@outlook.com";
        Customer customer = new Customer(
                null,
                oldEmail,
                "123456"
        );
        customerRepository.save(customer);

        // When
        customer.setEmail(newEmail);
        customer.setPassword("654321");
        customerRepository.save(customer);

        Customer storedCustomer = customerRepository.findByEmail(newEmail).get();

        // Then
        assertThat(storedCustomer.getId()).isNotNull();
        assertThat(storedCustomer.getEmail()).isEqualTo(newEmail);
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
