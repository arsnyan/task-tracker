package com.arsnyan.account.model.message;

public record UserEvent(
        Long userId,
        String username,
        UserEventType type
) {
    public static UserEvent ofCreatedType(Long userId, String username) {
        return new UserEvent(userId, username, UserEventType.CREATED);
    }
}
