package com.damian.xBank.customer.http.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record CustomerUpdateRequest(

        @NotNull(message = "Email cannot be empty.")
        @Email(message = "Invalid email.")
        String currentEmail,

        @Email
        String newEmail,

        @NotNull(message = "Current password cannot be empty.")
        String currentPassword,

        String newPassword) {
}
