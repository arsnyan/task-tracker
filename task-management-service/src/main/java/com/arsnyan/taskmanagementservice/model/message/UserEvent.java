package com.arsnyan.taskmanagementservice.model.message;

public record UserEvent(
        Long userId,
        String username,
        UserEventType type
) {}
