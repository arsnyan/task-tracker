package com.arsnyan.account.dto;

import com.arsnyan.account.model.User;
import com.arsnyan.account.model.UserRole;

public record UserResponseDto(
        Long userId,
        String username,
        String email,
        UserRole role
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}
