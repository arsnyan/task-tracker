package com.arsnyan.taskmanagementservice.dto;

import com.arsnyan.taskmanagementservice.model.Task;
import com.arsnyan.taskmanagementservice.model.TaskStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskDetailsResponseDto(
        Long taskId,
        String title,
        String content,
        TaskStatus status,
        ZonedDateTime finishedAt
) {
    public TaskDetailsResponseDto(Task task) {
        this(task.getTaskId(), task.getTitle(), task.getContent(), task.getStatus(), task.getFinishedAt());
    }
}
