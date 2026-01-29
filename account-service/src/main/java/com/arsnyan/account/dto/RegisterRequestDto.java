package com.arsnyan.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration request")
public record RegisterRequestDto(
        @Schema(description = "Unique username", example = "johndoe", minLength = 3, maxLength = 100)
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters long")
        String username,

        @Schema(description = "User email address", example = "john.doe@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "User password", example = "securePassword123", minLength = 6, maxLength = 100)
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Username must be between 6 and 100 characters long")
        String password
) {}
