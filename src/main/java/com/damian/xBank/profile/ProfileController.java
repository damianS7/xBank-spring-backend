package com.damian.xBank.profile;

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
    public ProfileController(ProfileService customerService) {
        this.profileService = customerService;
    }

    // endpoint para modificar profile al completo
    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updateProfile(@Validated @RequestBody ProfileUpdateRequest request) {
        Profile profile = profileService.updateProfile(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profile.toDTO());
    }

    // endpoint para modificar profile al completo
    @PatchMapping("/profile/{id}")
    public ResponseEntity<?> patchProfile(@Validated @RequestBody ProfileUpdateRequest request) {
        Profile profile = profileService.patchProfile(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(profile.toDTO());
    }

}

