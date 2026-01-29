package com.arsnyan.taskmanagementservice.dto;

import com.arsnyan.taskmanagementservice.model.Task;
import com.arsnyan.taskmanagementservice.model.TaskStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

@Schema(description = "Detailed task information")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskDetailsResponseDto(
        @Schema(description = "Unique task identifier", example = "1")
        Long taskId,

        @Schema(description = "Task title", example = "Complete project documentation")
        String title,

        @Schema(description = "Task content or description", example = "Write comprehensive documentation for the API endpoints")
        String content,

        @Schema(description = "Current task status", example = "IN_BACKLOG")
        TaskStatus status,

        @Schema(description = "Date and time when the task was completed", example = "2024-01-15T10:30:00Z")
        ZonedDateTime finishedAt
) {
    public TaskDetailsResponseDto(Task task) {
        this(task.getTaskId(), task.getTitle(), task.getContent(), task.getStatus(), task.getFinishedAt());
    }
}
