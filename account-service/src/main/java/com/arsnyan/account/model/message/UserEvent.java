package com.arsnyan.account.model.message;

public record UserEvent(
        Long userId,
        String username,
        String email,
        UserEventType type
) {
    public static UserEvent ofCreatedType(Long userId, String username, String email) {
        return new UserEvent(userId, username, email, UserEventType.CREATED);
    }
}
