package com.damian.xBank.customer.http.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CustomerUpdateRequest(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a well-formed email address.")
        String newEmail,

        @NotBlank
        String currentPassword,

        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must be at least 8 characters long, contain at least one uppercase letter, " +
                        "one number, and one special character.")
        String newPassword) {
}
