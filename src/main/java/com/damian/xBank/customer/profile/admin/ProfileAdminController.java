package com.damian.xBank.customer.profile.admin;

import com.damian.xBank.customer.profile.Profile;
import com.damian.xBank.customer.profile.ProfileDTO;
import com.damian.xBank.customer.profile.ProfileDTOMapper;
import com.damian.xBank.customer.profile.ProfileService;
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
public class ProfileAdminController {
    private final ProfileService profileService;

    @Autowired
    public ProfileAdminController(ProfileService profileService) {
        this.profileService = profileService;
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

