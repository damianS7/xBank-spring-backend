package com.damian.xBank.customer.profile;

import com.damian.xBank.common.exception.Exceptions;
import com.damian.xBank.common.utils.AuthUtils;
import com.damian.xBank.common.utils.ProfileUtils;
import com.damian.xBank.customer.Customer;
import com.damian.xBank.customer.CustomerGender;
import com.damian.xBank.customer.profile.exception.ProfileAuthorizationException;
import com.damian.xBank.customer.profile.exception.ProfileNotFoundException;
import com.damian.xBank.customer.profile.http.request.ProfileUpdateRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
import java.util.UUID;

@Service
public class ProfileService {
    // TODO move to ProfileAvatarService
    private final String PROFILE_IMAGE_PATH = "uploads/profile/images";
    private final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private final ProfileRepository profileRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public ProfileService(
            ProfileRepository profileRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder
    ) {
        this.profileRepository = profileRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * returns a profile
     *
     * @param profileId the profile id
     * @return Profile the profile
     */
    public Profile getProfile(Long profileId) {
        return profileRepository
                .findById(profileId)
                .orElseThrow(
                        () -> new ProfileNotFoundException(Exceptions.PROFILE.NOT_FOUND)
                );
    }

    // validations for file uploaded photos
    private void validatePhotoOrElseThrow(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ProfileAuthorizationException(
                    Exceptions.PROFILE.IMAGE.EMPTY_FILE
            );
        }

        if (!file.getContentType().startsWith("image/")) {
            throw new ProfileAuthorizationException(
                    Exceptions.PROFILE.IMAGE.ONLY_IMAGES_ALLOWED
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) { // 5 MB
            throw new ProfileAuthorizationException(
                    Exceptions.PROFILE.IMAGE.FILE_SIZE_LIMIT
            );
        }
    }

    // stores the image
    private void storeFile(MultipartFile file, String filename) {
        try {
            Path uploadPath = Paths.get(PROFILE_IMAGE_PATH);
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ProfileAuthorizationException(
                    Exceptions.PROFILE.IMAGE.UPLOAD_FAILED
            );
        }
    }

    // it updates the logged customer profile
    public Profile updateProfile(ProfileUpdateRequest request) {
        final Customer customerLogged = AuthUtils.getLoggedCustomer();

        // validate password
        AuthUtils.validatePasswordOrElseThrow(request.currentPassword(), customerLogged);

        return this.updateProfile(customerLogged.getProfile().getId(), request);
    }

    // it updates a profile by id
    public Profile updateProfile(Long profileId, ProfileUpdateRequest request) {
        // We get the profile we want to modify
        Profile profile = profileRepository
                .findById(profileId)
                .orElseThrow(
                        () -> new ProfileNotFoundException(
                                Exceptions.PROFILE.NOT_FOUND
                        )
                );

        final Customer customerLogged = AuthUtils.getLoggedCustomer();

        // if the logged user is not admin
        if (!AuthUtils.isAdmin(customerLogged)) {
            // we make sure that this profile belongs to the customer logged
            ProfileUtils.hasAuthorizationOrElseThrow(profile, customerLogged);
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
                default -> throw new ProfileAuthorizationException(
                        Exceptions.PROFILE.INVALID_FIELD
                );
            }
        });

        // we change the updateAt timestamp field
        profile.setUpdatedAt(Instant.now());

        return profileRepository.save(profile);
    }

    // returns the profile photo as Resource
    public Resource getPhoto(String filename) {
        Path filePath = Paths.get(PROFILE_IMAGE_PATH).resolve(filename).normalize();
        Resource resource = null;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        if (!resource.exists()) {
            throw new ProfileAuthorizationException(
                    Exceptions.PROFILE.IMAGE.NOT_FOUND
            );
        }
        return resource;
    }

    /**
     * It sets the customer profile photo
     */
    public Profile uploadPhoto(String currentPassword, MultipartFile file) {
        final Customer customerLogged = AuthUtils.getLoggedCustomer();

        // validate password
        AuthUtils.validatePasswordOrElseThrow(currentPassword, AuthUtils.getLoggedCustomer());

        // run file validations
        this.validatePhotoOrElseThrow(file);

        final String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        final String filename = UUID.randomUUID() + "." + extension;

        // saving file
        this.storeFile(file, filename);

        Map<String, Object> fieldsToUpdate = new HashMap<>();
        fieldsToUpdate.put("photoPath", filename);
        ProfileUpdateRequest patchRequest = new ProfileUpdateRequest(
                currentPassword,
                fieldsToUpdate
        );

        final Long profileId = customerLogged.getProfile().getId();

        return updateProfile(profileId, patchRequest);
    }
}
