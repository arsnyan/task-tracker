package com.arsnyan.taskmanagementservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new task")
public record TaskCreateRequestDto(
        @Schema(description = "Task title", example = "Complete project documentation", minLength = 1)
        @Size(min = 1)
        @NotNull
        String title,

        @Schema(description = "Task content or description", example = "Write comprehensive documentation for the API endpoints")
        String content
) {}
