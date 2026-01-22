package com.arsnyan.account.model;

import java.time.ZonedDateTime;

public record User(
        Long userId,
        String username,
        String email,
        String password,
        String role,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static User create(String username, String email, String encodedPassword) {
        return new User(null, username, email, encodedPassword, "USER", null, null);
    }

    public static User createAdmin(String username, String email, String encodedPassword) {
        return new User(null, username, email, encodedPassword, "ADMIN", null, null);
    }
}
