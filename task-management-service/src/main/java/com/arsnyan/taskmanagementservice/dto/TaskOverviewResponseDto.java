package com.arsnyan.taskmanagementservice.dto;

import com.arsnyan.taskmanagementservice.model.Task;
import com.arsnyan.taskmanagementservice.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Task overview information")
public record TaskOverviewResponseDto(
        @Schema(description = "Unique task identifier", example = "1")
        Long taskId,

        @Schema(description = "Task title", example = "Complete project documentation")
        String title,

        @Schema(description = "Current task status", example = "IN_BACKLOG")
        TaskStatus status
) {
    public TaskOverviewResponseDto(Task task) {
        this(task.getTaskId(), task.getTitle(), task.getStatus());
    }
}
