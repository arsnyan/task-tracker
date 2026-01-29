package com.arsnyan.taskmanagementservice.dto;

import com.arsnyan.taskmanagementservice.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update an existing task")
public record TaskUpdateRequestDto(
        @Schema(description = "Task ID to update", example = "1")
        @NotNull
        Long taskId,

        @Schema(description = "Updated task title", example = "Complete project documentation", minLength = 1)
        @Size(min = 1)
        String title,

        @Schema(description = "Updated task content or description", example = "Write comprehensive documentation for the API endpoints")
        String content,

        @Schema(description = "Updated task status", example = "IN_BACKLOG")
        TaskStatus status
) {}
