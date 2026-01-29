package com.arsnyan.account.dto;

import com.arsnyan.account.model.User;
import com.arsnyan.account.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User information response")
public record UserResponseDto(
        @Schema(description = "Unique user identifier", example = "1")
        Long userId,

        @Schema(description = "Username", example = "johndoe")
        String username,

        @Schema(description = "User email address", example = "john.doe@example.com")
        String email,

        @Schema(description = "User role", example = "USER")
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
