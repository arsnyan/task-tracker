package com.arsnyan.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User login request")
public record LoginRequestDto(
        @Schema(description = "Username or email address", example = "johndoe")
        @NotBlank(message = "Username or email is required")
        String username,

        @Schema(description = "User password", example = "securePassword123")
        @NotBlank(message = "Password is required")
        String password
) {}
