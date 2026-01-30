package com.arsnyan.taskmanagementservice.service;

import com.arsnyan.taskmanagementservice.dto.TaskCreateRequestDto;
import com.arsnyan.taskmanagementservice.dto.TaskDetailsResponseDto;
import com.arsnyan.taskmanagementservice.dto.TaskOverviewResponseDto;
import com.arsnyan.taskmanagementservice.dto.TaskUpdateRequestDto;
import com.arsnyan.taskmanagementservice.exception.NoSuchEntityException;
import com.arsnyan.taskmanagementservice.model.Task;
import com.arsnyan.taskmanagementservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<Task> findAllWithOwners() {
        return taskRepository.findAllWithOwners();
    }

    public List<TaskOverviewResponseDto> getAllByUsername(String username) {
        return taskRepository.findAllTasks(username).stream()
                .map(TaskOverviewResponseDto::new)
                .toList();
    }

    public TaskDetailsResponseDto getTaskById(String username, Long id) {
        var task = taskRepository.findByIdAndOwner(id, username)
                .orElseThrow(() -> taskDoesntExistException(id, username));

        return new TaskDetailsResponseDto(task);
    }

    @Transactional
    public TaskDetailsResponseDto createTask(String username, TaskCreateRequestDto dto) {
        var user = userService.getUser(username);
        var newTask = Task.create(user, dto.title(), dto.content());

        var createdTask = taskRepository.save(newTask);

        return new TaskDetailsResponseDto(createdTask);
    }

    @Transactional
    public TaskDetailsResponseDto updateTask(String username, Long taskId, TaskUpdateRequestDto dto) {
        var taskForUpdate = taskRepository.findByIdAndOwner(taskId, username)
                .orElseThrow(() -> taskDoesntExistException(taskId, username));

        if (dto.title() != null) {
            taskForUpdate.setTitle(dto.title());
        }

        if (dto.content() != null) {
            taskForUpdate.setContent(dto.content());
        }

        if (dto.status() != null) {
            taskForUpdate.setStatus(dto.status());
            taskRepository.finishTaskById(username, taskId, ZonedDateTime.now(clock));
        }

        taskRepository.saveAndFlush(taskForUpdate);

        return new TaskDetailsResponseDto(taskForUpdate);
    }

    @Transactional
    public void deleteTask(String username, Long taskId) {
        taskRepository.deleteByTaskIdAndOwnerUsername(taskId, username);
    }

    private NoSuchEntityException taskDoesntExistException(Long id, String username) {
        return new NoSuchEntityException("Task with ID=%s doesn't exist for %s".formatted(id, username));
    }
}
