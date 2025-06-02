package com.damian.xBank.customer.profile;

import com.damian.xBank.common.exception.PasswordMismatchException;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerGender;
import com.damian.xBank.customer.profile.exception.ProfileAuthorizationException;
import com.damian.xBank.customer.profile.exception.ProfileException;
import com.damian.xBank.customer.profile.exception.ProfileNotFoundException;
import com.damian.xBank.customer.profile.http.request.ProfileUpdateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileService profileService;

    private Customer customer;
    private final String RAW_PASSWORD = "123456";

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        profileRepository.deleteAll();

        customer = new Customer();
        customer.setId(2L);
        customer.setEmail("customer@test.com");
        customer.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        customer.getProfile().setId(5L);
        customer.getProfile().setNationalId("123456789Z");
        customer.getProfile().setFirstName("John");
        customer.getProfile().setLastName("Wick");
        customer.getProfile().setGender(CustomerGender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
        customer.getProfile().setCountry("USA");
        customer.getProfile().setAddress("fake ave");
        customer.getProfile().setPostalCode("050012");
        customer.getProfile().setPhotoPath("no photoPath");
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    void setUpContext(Customer customer) {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(customer);
    }

    @Test
    @DisplayName("Should update profile")
    void shouldUpdateProfile() {
        // given
        setUpContext(customer);

        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", "David");
        fields.put("lastName", "David");
        fields.put("birthdate", "1904-01-02");
        fields.put("gender", "MALE");
        fields.put("phone", "9199191919");
        fields.put("country", "Spain");
        fields.put("photoPath", "image.jpg");
        fields.put("nationalId", "234234234");
        ProfileUpdateRequest givenRequest = new ProfileUpdateRequest(
                RAW_PASSWORD,
                fields
        );

        // when
        when(profileRepository.findById(customer.getProfile().getId())).thenReturn(Optional.of(customer.getProfile()));
        when(profileRepository.save(any(Profile.class))).thenReturn(customer.getProfile());

        Profile result = profileService.updateProfile(givenRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo(givenRequest.fieldsToUpdate().get("firstName"));
        assertThat(result.getLastName()).isEqualTo(givenRequest.fieldsToUpdate().get("lastName"));
        assertThat(result.getPhone()).isEqualTo(givenRequest.fieldsToUpdate().get("phone"));
        assertThat(result.getCountry()).isEqualTo(givenRequest.fieldsToUpdate().get("country"));
        assertThat(result.getBirthdate().toString()).isEqualTo(givenRequest.fieldsToUpdate().get("birthdate"));
        assertThat(result.getGender().toString()).isEqualTo(givenRequest.fieldsToUpdate().get("gender"));
        assertThat(result.getPhotoPath()).isEqualTo(givenRequest.fieldsToUpdate().get("photoPath"));
        assertThat(result.getNationalId()).isEqualTo(givenRequest.fieldsToUpdate().get("nationalId"));
        verify(profileRepository, times(1)).save(customer.getProfile());
    }

    @Test
    @DisplayName("Should not update profile when password is wrong")
    void shouldNotUpdateProfileWhenPasswordIsWrong() {
        // given
        setUpContext(customer);

        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", "David");
        ProfileUpdateRequest givenRequest = new ProfileUpdateRequest(
                "wrongPassword1",
                fields
        );

        // when
        PasswordMismatchException exception = assertThrows(
                PasswordMismatchException.class,
                () -> profileService.updateProfile(givenRequest)
        );

        // Then
        assertEquals(PasswordMismatchException.PASSWORD_MISMATCH, exception.getMessage());
    }

    @Test
    @DisplayName("Should not update profile when profile not found")
    void shouldNotUpdateProfileWhenProfileNotFound() {
        // given
        setUpContext(customer);

        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", "David");
        ProfileUpdateRequest givenRequest = new ProfileUpdateRequest(
                RAW_PASSWORD,
                fields
        );

        // when
        when(profileRepository.findById(customer.getProfile().getId())).thenReturn(Optional.empty());
        ProfileNotFoundException exception = assertThrows(
                ProfileNotFoundException.class,
                () -> profileService.updateProfile(givenRequest)
        );

        // Then
        assertEquals(ProfileException.NOT_FOUND, exception.getMessage());
    }

    @Test
    @DisplayName("Should not update profile when profile not found")
    void shouldNotUpdateProfileWhenProfileNotYours() {
        // given
        setUpContext(customer);

        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", "David");
        ProfileUpdateRequest givenRequest = new ProfileUpdateRequest(
                RAW_PASSWORD,
                fields
        );

        Profile givenProfile = new Profile();
        givenProfile.setCustomer(new Customer(5L, "customer@test.com", "12345"));

        // when
        when(profileRepository.findById(customer.getProfile().getId())).thenReturn(Optional.of(givenProfile));
        ProfileAuthorizationException exception = assertThrows(
                ProfileAuthorizationException.class,
                () -> profileService.updateProfile(givenRequest)
        );

        // Then
        assertEquals(ProfileException.AUTHORIZATION.ACCESS_FORBIDDEN, exception.getMessage());
    }

    @Test
    @DisplayName("Should not update profile when profile not found")
    void shouldNotUpdateProfileWhenInvalidField() {
        // given
        setUpContext(customer);

        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", "David");
        fields.put("fakeField", "1234");
        ProfileUpdateRequest givenRequest = new ProfileUpdateRequest(
                RAW_PASSWORD,
                fields
        );

        // when
        when(profileRepository.findById(customer.getProfile().getId())).thenReturn(Optional.of(customer.getProfile()));
        ProfileAuthorizationException exception = assertThrows(
                ProfileAuthorizationException.class,
                () -> profileService.updateProfile(givenRequest)
        );

        // Then
        assertEquals(ProfileException.INVALID_FIELD, exception.getMessage());
    }

    // TODO test upload profile photo
    // TODO test get profile photo
}
