package com.damian.xBank.customer.profile;

import com.damian.xBank.customer.profile.http.request.ProfilePatchRequest;
import com.damian.xBank.customer.profile.http.request.ProfileUpdateRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RestController
public class ProfileController {
    private final ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PatchMapping("/profiles")
    public ResponseEntity<?> patchCustomerProfile(
            @Validated @RequestBody
            ProfilePatchRequest request
    ) {
        Profile profile = profileService.patchCustomerProfile(request);
        ProfileDTO profileDTO = ProfileDTOMapper.toProfileDTO(profile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profileDTO);
    }

    // endpoint to modify a profile
    @PutMapping("/admin/profiles/{id}")
    public ResponseEntity<?> updateProfile(
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

    // endpoint to modify some parts of the profile
    @PatchMapping("/admin/profiles/{id}")
    public ResponseEntity<?> patchProfile(
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

