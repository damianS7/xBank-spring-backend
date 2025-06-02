package com.damian.xBank.customer.profile;

pimport com.damian.xBank.customer.profile.http.request.ProfileUpdateRequest;
import jakarta.validation.constraints.NotBlank;
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
    public ResponseEntity<?> updateLoggedCustomerProfile(
            @Validated @RequestBody
            ProfileUpdateRequest request
    ) {
        Profile profile = profileService.updateProfile(request);
        ProfileDTO profileDTO = ProfileDTOMapper.toProfileDTO(profile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileDTO);
    }

    // TODO move logic to service
    // endpoint to get the logged customer profile photo
    @GetMapping("/customers/me/profile/photo/{filename:.+}")
    public ResponseEntity<?> getLoggedCustomerPhoto(
            @PathVariable @NotBlank
            String filename
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
    public ResponseEntity<?> uploadLoggedCustomerPhoto(
            @RequestParam("currentPassword") @NotBlank String currentPassword,
            @RequestParam("file") MultipartFile file
    ) {
        Profile profile = profileService.uploadPhoto(currentPassword, file);
        ProfileDTO profileDTO = ProfileDTOMapper.toProfileDTO(profile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileDTO);
    }
}

