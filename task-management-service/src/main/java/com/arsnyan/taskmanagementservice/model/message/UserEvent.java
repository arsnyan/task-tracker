package com.arsnyan.taskmanagementservice.model.message;

public record UserEvent(
        Long userId,
        String username,
        String email,
        UserEventType type
) {}
