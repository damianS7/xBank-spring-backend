package com.damian.xBank.auth;

import com.damian.xBank.customer.CustomerRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class AuthenticationRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    private Faker faker;


    @BeforeEach
    void setUp() {
        faker = new Faker();
        customerRepository.deleteAll();
    }

    @Test
    void test() {
    }


}
