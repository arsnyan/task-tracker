package com.arsnyan.taskmanagementservice.model.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskUpdatedEvent(
        @NotNull
        Long taskId,

        String title,

        Boolean isCompleted
) {}
