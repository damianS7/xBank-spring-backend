package com.damian.xBank.profile;

import com.damian.xBank.auth.exception.AuthorizationException;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.exception.CustomerException;
import com.damian.xBank.profile.http.ProfileUpdateRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
     * @param request the fields to change
     * @return the profile updated
     */
    public Profile updateProfile(ProfileUpdateRequest request) {
        // We get the customer logged in the context
        Customer customerLogged = (Customer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // before making any changes we check that the user sent the current password
        if (!bCryptPasswordEncoder.matches(request.currentPassword(), customerLogged.getPassword())) {
            throw new CustomerException("Password does not match.");
        }

        // the profile id the current customer has
        Long profileId = customerLogged.getProfile().getId();

        // We get the profile we want to modify
        Profile profile = profileRepository.findById(profileId).orElseThrow(
                () -> new ProfileException("Profile cannot be found.")
        );

        // we make sure that this profile belongs to the customer logged
        if (!profile.getCustomerId().equals(customerLogged.getId())) {
            throw new AuthorizationException("This profile does not belongs to the logged user.");
        }

        // we make the changes into the profile
        profile.setNationalId(request.nationalId());
        profile.setName(request.name());
        profile.setSurname(request.surname());
        profile.setGender(request.gender());
        profile.setBirthdate(request.birthdate());
        profile.setCountry(request.country());
        profile.setPhone(request.phone());
        profile.setAddress(request.address());
        profile.setPostalCode(request.postalCode());
        profile.setPhoto(request.photo());

        return profileRepository.save(profile);
    }

    public Profile patchProfile(ProfileUpdateRequest request) {
        // Comprobamos que el usuario que esta intentando modifiicar el perfil
        // sea el owner del perfil
        Customer customerLogged = (Customer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Comprueba que el perfil que se intenta modificar pertenezca al usuario logeado
        if (!customerLogged.getId().equals(request.customerId())) {
            throw new AuthorizationException("Profile does not belong to this user.");
        }

        // Antes de cambiar comprobamos que las password antiguas coincidan
        if (!bCryptPasswordEncoder.matches(request.currentPassword(), customerLogged.getPassword())) {
            throw new CustomerException("Password does not match.");
        }

        // Obtenemos el nombre del usuario logeado que envia la peticion
        Profile profile = profileRepository.findById(request.id()).orElseThrow(
                () -> new ProfileException("Profile cannot be found.")
        );

        if (profile.getName() != null) {
            profile.setName(request.name());
        }

        if (profile.getSurname() != null) {
            profile.setSurname(request.surname());
        }

        if (profile.getGender() != null) {
            profile.setGender(request.gender());
        }

        if (profile.getCountry() != null) {
            profile.setCountry(request.country());
        }

        if (profile.getAddress() != null) {
            profile.setAddress(request.address());
        }

        if (profile.getPostalCode() != null) {
            profile.setPostalCode(request.postalCode());
        }

        if (profile.getBirthdate() != null) {
            profile.setBirthdate(request.birthdate());
        }

        if (profile.getPhoto() != null) {
            profile.setPhoto(request.photo());
        }

        if (profile.getPhone() != null) {
            profile.setPhone(request.phone());
        }

        if (profile.getNationalId() != null) {
            profile.setNationalId(request.nationalId());
        }

        return profileRepository.save(profile);
    }
}
