package com.damian.xBank.customer.profile;

import com.damian.xBank.customer.profile.http.request.ProfilePatchRequest;
import com.damian.xBank.customer.profile.http.request.ProfileUpdateRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

@RequestMapping("/api/v1")
@RestController
public class ProfileController {
    private final ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // endpoint to modify the logged customer profile
    @PatchMapping("/customers/me/profile")
    public ResponseEntity<?> patchLoggedCustomerProfile(
            @Validated @RequestBody
            ProfilePatchRequest request
    ) {
        Profile profile = profileService.patchCustomerProfile(request);
        ProfileDTO profileDTO = ProfileDTOMapper.toProfileDTO(profile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileDTO);
    }

    @GetMapping("/customers/me/profile/photo/{filename:.+}")
    public ResponseEntity<?> getLoggedCustomerPhoto(
            @PathVariable String filename
    ) {
        Resource resource = profileService.getPhoto(filename);

        String contentType = null;
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }


        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    // endpoint to upload profile photo
    @PostMapping("/customers/me/profile/photo")
    public ResponseEntity<?> postLoggedCustomerPhoto(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("file") MultipartFile file
    ) {
        Profile profile = profileService.uploadPhoto(currentPassword, file);
        ProfileDTO profileDTO = ProfileDTOMapper.toProfileDTO(profile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileDTO);
    }

    // endpoint to modify the entire profile
    // FIXME cambiar a /admin/customers/{id}/profile ?
    @PutMapping("/admin/profiles/{id}")
    public ResponseEntity<?> putCustomerProfile(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            ProfileUpdateRequest request
    ) {
        Profile profile = profileService.updateProfile(id, request);
        ProfileDTO profileDTO = ProfileDTOMapper.toProfileDTO(profile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileDTO);
    }

    // endpoint to partially modify the profile
    @PatchMapping("/admin/profiles/{id}")
    public ResponseEntity<?> patchCustomerProfile(
            @PathVariable @NotNull @Positive
            Long id,
            @Validated @RequestBody
            ProfilePatchRequest request
    ) {
        Profile profile = profileService.patchProfile(id, request);
        ProfileDTO profileDTO = ProfileDTOMapper.toProfileDTO(profile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileDTO);
    }

}

