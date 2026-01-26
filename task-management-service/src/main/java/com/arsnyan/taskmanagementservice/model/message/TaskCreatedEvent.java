package com.arsnyan.taskmanagementservice.model.message;

import jakarta.validation.constraints.NotNull;

public record TaskCreatedEvent(
        @NotNull
        Long taskId,

        String title
) {}
