package com.damian.xBank.customer.http.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CustomerUpdateRequest(
        @NotNull
        @NotBlank
        @Email
        String currentEmail,

        @Email
        String newEmail,

        @NotNull
        @NotBlank
        String currentPassword,

        String newPassword) {
}
