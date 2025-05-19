package com.damian.xBank.customer.profile;

import com.damian.xBank.auth.exception.AuthorizationException;
import com.damian.xBank.common.exception.PasswordMismatchException;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerGender;
import com.damian.xBank.customer.CustomerRole;
import com.damian.xBank.customer.profile.exception.ProfileException;
import com.damian.xBank.customer.profile.exception.ProfileNotFoundException;
import com.damian.xBank.customer.profile.http.request.ProfilePatchRequest;
import com.damian.xBank.customer.profile.http.request.ProfileUpdateRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public ProfileService(ProfileRepository profileRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.profileRepository = profileRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public Resource getPhoto(String filename) {
        Path filePath = Paths.get("uploads/avatars").resolve(filename).normalize();
        System.out.println(filePath);
        Resource resource = null;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        if (!resource.exists()) {
            //            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Archivo no encontrado");
        }

        return resource;

    }

    /**
     * It sets the customer profile photo
     */
    public Profile uploadPhoto(String currentPassword, MultipartFile file) {
        //        if (file.isEmpty()) {
        //            throw new IllegalArgumentException("El archivo está vacío");
        //        }
        //
        //        if (!file.getContentType().startsWith("image/")) {
        //            throw new IllegalArgumentException("Solo se permiten archivos de imagen");
        //        }
        //
        //        if (file.getSize() > 5 * 1024 * 1024) { // 5 MB
        //            throw new IllegalArgumentException("El archivo no debe superar los 5 MB");
        //        }
        Customer customerLogged = (Customer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final Long profileId = customerLogged.getProfile().getId();

        // ...
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + extension;

        Map<String, Object> fieldsToUpdate = new HashMap<>();

        // Guardar el archivo localmente (puedes adaptarlo a guardar en S3, DB, etc.)
        try {
            Path uploadPath = Paths.get("uploads/avatars");
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            fieldsToUpdate.put("photoPath", filename);
        } catch (IOException e) {
            throw new ProfileException("Error al subir archivo");
        }


        ProfilePatchRequest patchRequest = new ProfilePatchRequest(
                currentPassword,
                fieldsToUpdate
        );

        return patchProfile(profileId, patchRequest);
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
        fieldsToUpdate.put("firstName", request.name());
        fieldsToUpdate.put("lastName", request.surname());
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

    public Profile patchCustomerProfile(ProfilePatchRequest request) {
        return this.patchProfile(null, request);
    }

    public Profile patchProfile(Long profileId, ProfilePatchRequest request) {
        // We get the customer logged in the context
        Customer customerLogged = (Customer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // if profiledId is null, then is a customer modifying its own profile.
        if (profileId == null) {
            profileId = Optional.ofNullable(customerLogged.getProfile())
                                .map(Profile::getId)
                                .orElseThrow(() -> new ProfileException("Cannot access to customer profile.")
                                );
        }

        // We get the profile we want to modify
        Profile profile = profileRepository
                .findById(profileId)
                .orElseThrow(
                        ProfileNotFoundException::new
                );

        // if the logged user is not admin
        if (!customerLogged.getRole().equals(CustomerRole.ADMIN)) {
            // before making any changes we check that the user sent the current password.
            if (!bCryptPasswordEncoder.matches(request.currentPassword(), customerLogged.getPassword())) {
                throw new PasswordMismatchException();
            }

            // we make sure that this profile belongs to the customer logged
            if (!profile.getCustomerId().equals(customerLogged.getId())) {
                throw new AuthorizationException("You are not the owner of this profile.");
            }
        }

        // we iterate over the fields (if any)
        request.fieldsToUpdate().forEach((key, value) -> {
            switch (key) {
                case "firstName" -> profile.setFirstName((String) value);
                case "lastName" -> profile.setLastName((String) value);
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

        // we change the updateAt timestamp field
        profile.setUpdatedAt(Instant.now());

        return profileRepository.save(profile);
    }
}
