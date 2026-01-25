package com.arsnyan.account.dto;

public record AuthResponseDto(
        String token,
        String tokenType,
        long expiresIn,
        UserResponseDto user
) {
    public static AuthResponseDto of(String token, long expiresInMs, UserResponseDto user) {
        return new AuthResponseDto(token, "Bearer",  expiresInMs / 1000, user);
    }
}
