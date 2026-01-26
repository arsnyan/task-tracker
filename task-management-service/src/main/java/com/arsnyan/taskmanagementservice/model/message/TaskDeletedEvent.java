package com.arsnyan.taskmanagementservice.model.message;

import jakarta.validation.constraints.NotNull;

public record TaskDeletedEvent(@NotNull Long taskId) {}
