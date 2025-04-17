package com.damian.xBank.profile;

import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Faker faker;
    private Customer customer;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        profileRepository.deleteAll();

        customer = new Customer();
        customer.setEmail("david@gmail.com");
        customer.setPassword("123456");
        customer.getProfile().setNationalId("123456789Z");
        customer.getProfile().setName("david");
        customer.getProfile().setSurname("white");
        customer.getProfile().setPhone("123 123 123");
        customer.getProfile().setGender(Gender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
        customer.getProfile().setCountry("USA");
        customer.getProfile().setAddress("fake av");
        customer.getProfile().setPostalCode("501200");
        customer.getProfile().setPhotoPath("/images/photoPath.jpg");

        customerRepository.save(customer);
    }

    @Test
    void shouldFindProfile() {
        // given
        Long profileId = customer.getProfile().getId();

        // when
        Profile profile = profileRepository.findById(profileId).orElseThrow();

        // then
        assertThat(profile.getId()).isNotNull();
        assertThat(profile.getCustomerId()).isEqualTo(customer.getId());
        assertThat(profile.getName()).isEqualTo(customer.getProfile().getName());
    }

    @Test
    void shouldNotFindProfile() {
        // given
        Long profileId = -1L;

        // when
        boolean profileExists = profileRepository.existsById(profileId);

        // then
        assertThat(profileExists).isFalse();
    }

    @Test
    void shouldThrowWhenProfileIdIsNull() {
        // given
        Long profileId = null;

        // when
        // then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileRepository.findById(profileId)
        );
    }

    @Test
    void shouldUpdateProfile() {
        // given
        Long profileId = customer.getProfile().getId();
        final String newName = "Ronald";

        // when
        customer.getProfile().setName(newName);
        profileRepository.save(customer.getProfile());
        Profile profile = profileRepository.findById(profileId).orElseThrow();

        // then
        assertThat(profile.getName()).isEqualTo(newName);
    }
}
