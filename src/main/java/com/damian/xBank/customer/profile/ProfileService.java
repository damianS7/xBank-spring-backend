package com.damian.xBank.customer.profile;

import com.damian.xBank.auth.exception.AuthorizationException;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.exception.CustomerException;
import com.damian.xBank.customer.profile.http.request.ProfilePatchRequest;
import com.damian.xBank.customer.profile.http.request.ProfileUpdateRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public ProfileService(ProfileRepository profileRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.profileRepository = profileRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * It updates the whole customer profile
     *
     * @param profileId the id of the profile to update
     * @param request   the fields to change
     * @return the profile updated
     */
    public Profile updateProfile(Long profileId, ProfileUpdateRequest request) {
        Map<String, Object> fieldsToUpdate = new HashMap<>();
        fieldsToUpdate.put("name", request.name());
        fieldsToUpdate.put("surname", request.surname());
        fieldsToUpdate.put("phone", request.phone());
        fieldsToUpdate.put("address", request.address());
        fieldsToUpdate.put("country", request.country());
        fieldsToUpdate.put("postalCode", request.postalCode());
        fieldsToUpdate.put("photoPath", request.photoPath());
        fieldsToUpdate.put("nationalId", request.nationalId());
        fieldsToUpdate.put("gender", request.gender().toString());
        fieldsToUpdate.put("birthdate", request.birthdate().toString());

        ProfilePatchRequest patchRequest = new ProfilePatchRequest(
                request.currentPassword(),
                fieldsToUpdate
        );

        return patchProfile(profileId, patchRequest);
    }

    public Profile patchProfile(Long profileId, ProfilePatchRequest request) {
        // We get the customer logged in the context
        Customer customerLogged = (Customer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // before making any changes we check that the user sent the current password
        if (!bCryptPasswordEncoder.matches(request.currentPassword(), customerLogged.getPassword())) {
            throw new CustomerException("Password does not match.");
        }

        // We get the profile we want to modify
        Profile profile = profileRepository.findById(profileId).orElseThrow(
                () -> new ProfileException("Profile cannot be found.")
        );

        // we make sure that this profile belongs to the customer logged
        if (!profile.getCustomerId().equals(customerLogged.getId())) {
            throw new AuthorizationException("This profile does not belongs to the logged user.");
        }

        // we iterate over the fields (if any)
        request.fieldsToUpdate().forEach((key, value) -> {
            switch (key) {
                case "name" -> profile.setName((String) value);
                case "surname" -> profile.setSurname((String) value);
                case "phone" -> profile.setPhone((String) value);
                case "address" -> profile.setAddress((String) value);
                case "country" -> profile.setCountry((String) value);
                case "postalCode" -> profile.setPostalCode((String) value);
                case "photoPath" -> profile.setPhotoPath((String) value);
                case "nationalId" -> profile.setNationalId((String) value);
                case "gender" -> profile.setGender(CustomerGender.valueOf((String) value));
                case "birthdate" -> profile.setBirthdate(LocalDate.parse((String) value));
                default -> throw new ProfileException("Field '" + key + "' is not updatable.");
            }
        });

        return profileRepository.save(profile);
    }
}
