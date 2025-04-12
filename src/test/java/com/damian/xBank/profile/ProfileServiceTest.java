package com.damian.xBank.profile;

import com.damian.xBank.auth.exception.AuthorizationException;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerRepository;
import com.damian.xBank.customer.exception.CustomerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

// Habilita Mockito en JUnit 5
@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private ProfileService profileService;

    private Customer customer;
    private final String rawPassword = "123456";
    private final String encodedPassword = "123;456$";

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(profileRepository, bCryptPasswordEncoder);
        profileRepository.deleteAll();

        customer = new Customer();
        customer.setId(2L);
        customer.setEmail("alice@test.com");
        customer.setPassword(encodedPassword);
        customer.getProfile().setId(5L);
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

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                customer, null, Collections.emptyList()));
    }

    @Test
    void shouldUpdateProfile() {
        // given
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest(
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

        // when
        when(bCryptPasswordEncoder.matches(this.rawPassword, customer.getPassword())).thenReturn(true);
        when(profileRepository.findById(updateRequest.id())).thenReturn(Optional.of(customer.getProfile()));
        profileService.updateProfile(updateRequest);

        // Then
        verify(profileRepository, times(1)).save(customer.getProfile());
        assertThat(customer.getProfile().getName()).isEqualTo(updateRequest.name());
    }

    @Test
    void shouldNotUpdateProfileWhenPasswordDoesNotMatch() {
        // given
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest(
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

        // when
        when(bCryptPasswordEncoder.matches(this.rawPassword, customer.getPassword())).thenReturn(false);

        CustomerException exception = assertThrows(CustomerException.class,
                () -> profileService.updateProfile(updateRequest)
        );

        // Then
        verify(profileRepository, times(0)).save(customer.getProfile());
        assertEquals("Password does not match.", exception.getMessage());
    }

    @Test
    void shouldNotUpdateProfileWhenProfileDoesNotBelongToCustomer() {
        // given
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest(
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

        Profile profileFromOtherUser = new Profile();
        profileFromOtherUser.setCustomer(new Customer(4L, "ronald@test.com", "1234"));

        // when
        when(bCryptPasswordEncoder.matches(this.rawPassword, customer.getPassword())).thenReturn(true);
        when(profileRepository.findById(anyLong())).thenReturn(Optional.of(profileFromOtherUser));
        AuthorizationException exception = assertThrows(AuthorizationException.class,
                () -> profileService.updateProfile(updateRequest)
        );

        // Then
        verify(profileRepository, times(0)).save(customer.getProfile());
        assertEquals("This profile does not belongs to the logged user.", exception.getMessage());
    }
}
