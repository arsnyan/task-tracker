package com.arsnyan.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT token and user information")
public record AuthResponseDto(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Token type", example = "Bearer")
        String tokenType,

        @Schema(description = "Token expiration time in seconds", example = "7200")
        long expiresIn,

        @Schema(description = "Authenticated user information")
        UserResponseDto user
) {
    public static AuthResponseDto of(String token, long expiresInMs, UserResponseDto user) {
        return new AuthResponseDto(token, "Bearer",  expiresInMs / 1000, user);
    }
}
