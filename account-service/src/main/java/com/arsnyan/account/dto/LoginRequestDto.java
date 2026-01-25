package com.arsnyan.account.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "Username or email is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {}
