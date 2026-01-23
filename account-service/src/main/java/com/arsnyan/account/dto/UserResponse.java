package com.arsnyan.account.dto;

import com.arsnyan.account.model.User;
import com.arsnyan.account.model.UserRole;

public record UserResponse(
        Long userId,
        String username,
        String email,
        UserRole role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}
