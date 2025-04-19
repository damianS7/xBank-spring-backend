package com.damian.xBank.customer.profile;

import com.damian.xBank.auth.exception.AuthorizationException;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.exception.CustomerException;
import com.damian.xBank.customer.profile.http.request.ProfilePatchRequest;
import com.damian.xBank.customer.profile.http.request.ProfileUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private ProfileService profileService;

    private Customer customer;
    private final String rawPassword = "123456";

    @BeforeEach
    void setUp() {
        profileRepository.deleteAll();
        profileService = new ProfileService(profileRepository, bCryptPasswordEncoder);

        customer = new Customer();
        customer.setId(2L);
        customer.setEmail("customer@test.com");
        customer.setPassword("encryptedPassword");
        customer.getProfile().setId(5L);
        customer.getProfile().setNationalId("123456789Z");
        customer.getProfile().setName("John");
        customer.getProfile().setSurname("Wick");
        customer.getProfile().setGender(CustomerGender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
        customer.getProfile().setCountry("USA");
        customer.getProfile().setAddress("fake ave");
        customer.getProfile().setPostalCode("050012");
        customer.getProfile().setPhotoPath("no photoPath");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        customer, null, Collections.emptyList()
                )
        );
    }

    @Test
    void shouldUpdateProfile() {
        // given
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest(
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

        // when
        when(bCryptPasswordEncoder.matches(this.rawPassword, customer.getPassword())).thenReturn(true);
        when(profileRepository.findById(customer.getProfile().getId())).thenReturn(Optional.of(customer.getProfile()));
        when(profileRepository.save(any(Profile.class))).thenReturn(customer.getProfile());

        Profile result = profileService.updateProfile(customer.getProfile().getId(), updateRequest);

        // Then
        verify(profileRepository, times(1)).save(customer.getProfile());
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(updateRequest.name());
        assertThat(result.getSurname()).isEqualTo(updateRequest.surname());
        assertThat(result.getPhone()).isEqualTo(updateRequest.phone());
        assertThat(result.getCountry()).isEqualTo(updateRequest.country());
        assertThat(result.getNationalId()).isEqualTo(updateRequest.nationalId());
        assertThat(result.getBirthdate()).isEqualTo(updateRequest.birthdate());
        assertThat(result.getPhotoPath()).isEqualTo(updateRequest.photoPath());
    }

    @Test
    void shouldPatchProfile() {
        // given
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", "alice");
        fields.put("surname", "white");
        fields.put("phone", "999 999 999");
        fields.put("birthdate", "1983-03-13");
        fields.put("gender", "FEMALE");

        ProfilePatchRequest patchProfile = new ProfilePatchRequest(
                this.rawPassword,
                fields
        );

        // when
        when(bCryptPasswordEncoder.matches(this.rawPassword, customer.getPassword())).thenReturn(true);
        when(profileRepository.findById(customer.getProfile().getId())).thenReturn(Optional.of(customer.getProfile()));
        when(profileRepository.save(any(Profile.class))).thenReturn(customer.getProfile());

        Profile result = profileService.patchProfile(customer.getProfile().getId(), patchProfile);

        // Then
        verify(profileRepository, times(1)).save(customer.getProfile());
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(patchProfile.fieldsToUpdate().get("name"));
        assertThat(result.getSurname()).isEqualTo(patchProfile.fieldsToUpdate().get("surname"));
        assertThat(result.getPhone()).isEqualTo(patchProfile.fieldsToUpdate().get("phone"));
        assertThat(result.getBirthdate().toString()).isEqualTo(patchProfile.fieldsToUpdate().get("birthdate"));
        assertThat(result.getGender().toString()).isEqualTo(patchProfile.fieldsToUpdate().get("gender"));
    }

    @Test
    void shouldNotPatchProfileWhenInvalidFields() {
        // given
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", "alice");
        fields.put("surname", "white");
        fields.put("FAKE FIELD", "999 999 999");

        ProfilePatchRequest patchProfile = new ProfilePatchRequest(
                this.rawPassword,
                fields
        );

        // when
        when(bCryptPasswordEncoder.matches(this.rawPassword, customer.getPassword())).thenReturn(true);
        when(profileRepository.findById(customer.getProfile().getId())).thenReturn(Optional.of(customer.getProfile()));

        // Then
        ProfileException exception = assertThrows(ProfileException.class,
                () -> profileService.patchProfile(customer.getProfile().getId(), patchProfile)
        );
    }

    @Test
    void shouldNotUpdateProfileWhenPasswordDoesNotMatch() {
        // given
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest(
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

        // when
        when(bCryptPasswordEncoder.matches(this.rawPassword, customer.getPassword())).thenReturn(false);

        CustomerException exception = assertThrows(CustomerException.class,
                () -> profileService.updateProfile(customer.getProfile().getId(), updateRequest)
        );

        // Then
        verify(profileRepository, times(0)).save(customer.getProfile());
        assertEquals("Password does not match.", exception.getMessage());
    }

    @Test
    void shouldNotUpdateProfileWhenProfileDoesNotBelongToCustomer() {
        // given
        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest(
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

        Profile profileFromOtherUser = new Profile();
        profileFromOtherUser.setCustomer(new Customer(4L, "ronald@test.com", "1234"));

        // when
        when(bCryptPasswordEncoder.matches(this.rawPassword, customer.getPassword())).thenReturn(true);
        when(profileRepository.findById(anyLong())).thenReturn(Optional.of(profileFromOtherUser));
        AuthorizationException exception = assertThrows(AuthorizationException.class,
                () -> profileService.updateProfile(customer.getProfile().getId(), updateRequest)
        );

        // Then
        verify(profileRepository, times(0)).save(customer.getProfile());
        assertEquals("This profile does not belongs to the logged user.", exception.getMessage());
    }
}
