package com.arsnyan.taskmanagementservice.dto;

import com.arsnyan.taskmanagementservice.model.TaskStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskUpdateRequestDto(
        @NotNull
        Long taskId,

        @Size(min = 1)
        String title,

        String content,

        TaskStatus status
) {}
