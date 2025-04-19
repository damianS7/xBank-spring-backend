package com.damian.xBank.customer.profile.http.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record ProfilePatchRequest(

        @NotBlank
        String currentPassword,
        Map<String, Object> fieldsToUpdate

) {
}
