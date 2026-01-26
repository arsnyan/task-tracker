package com.arsnyan.taskmanagementservice.dto;

import com.arsnyan.taskmanagementservice.model.Task;
import com.arsnyan.taskmanagementservice.model.TaskStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.ZonedDateTime;

public record TaskOverviewResponseDto(
        Long taskId,
        String title,
        TaskStatus status
) {
    public TaskOverviewResponseDto(Task task) {
        this(task.getTaskId(), task.getTitle(), task.getStatus());
    }
}
