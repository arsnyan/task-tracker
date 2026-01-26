package com.arsnyan.taskmanagementservice.service;

import com.arsnyan.taskmanagementservice.config.KafkaConfiguration;
import com.arsnyan.taskmanagementservice.dto.TaskCreateRequestDto;
import com.arsnyan.taskmanagementservice.dto.TaskDetailsResponseDto;
import com.arsnyan.taskmanagementservice.dto.TaskOverviewResponseDto;
import com.arsnyan.taskmanagementservice.dto.TaskUpdateRequestDto;
import com.arsnyan.taskmanagementservice.exception.NoSuchEntityException;
import com.arsnyan.taskmanagementservice.model.Task;
import com.arsnyan.taskmanagementservice.model.TaskStatus;
import com.arsnyan.taskmanagementservice.model.message.TaskCreatedEvent;
import com.arsnyan.taskmanagementservice.model.message.TaskDeletedEvent;
import com.arsnyan.taskmanagementservice.model.message.TaskIdentifier;
import com.arsnyan.taskmanagementservice.model.message.TaskUpdatedEvent;
import com.arsnyan.taskmanagementservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final KafkaTemplate<TaskIdentifier, Object> kafkaTemplate;

    public List<TaskOverviewResponseDto> getAllByUsername(String username) {
        return taskRepository.findAllTasks(username).stream()
                .map(TaskOverviewResponseDto::new)
                .toList();
    }

    public TaskDetailsResponseDto getTaskById(String username, Long id) {
        var task = taskRepository.finishTaskById(username, id)
                .orElseThrow(() -> taskDoesntExistException(id, username));

        return new TaskDetailsResponseDto(task);
    }

    @Transactional
    public TaskDetailsResponseDto createTask(Jwt jwt, TaskCreateRequestDto dto) {
        var user = userService.getUser(jwt.getClaimAsString("name"));
        var newTask = Task.create(user, dto.title(), dto.content());

        var createdTask = taskRepository.save(newTask);
        sendTaskCreatedEvent(jwt.getSubject(), createdTask.getTaskId(), createdTask.getTitle());

        return new TaskDetailsResponseDto(createdTask);
    }

    private void sendTaskCreatedEvent(String email, Long taskId, String title) {
        var taskIdentifier = new TaskIdentifier(email);
        var event = new TaskCreatedEvent(taskId, title);

        kafkaTemplate.send(KafkaConfiguration.TASK_TOPIC_NAME, taskIdentifier, event);
    }

    @Transactional
    public TaskDetailsResponseDto updateTask(Jwt jwt, TaskUpdateRequestDto dto) {
        var username = jwt.getClaimAsString("user");
        var user = userService.getUser(username);
        var taskForUpdate = taskRepository.findByIdAndOwner(dto.taskId(), user)
                .orElseThrow(() -> taskDoesntExistException(dto.taskId(), username));

        if (dto.title() != null) {
            taskForUpdate.setTitle(dto.title());
        }

        if (dto.content() != null) {
            taskForUpdate.setContent(dto.content());
        }

        if (dto.status() != null) {
            taskForUpdate.setStatus(dto.status());
            taskRepository.finishTaskById(username, dto.taskId());
        }

        var updatedTask = taskRepository.saveAndFlush(taskForUpdate);
        sendTaskUpdatedEvent(jwt.getSubject(), updatedTask.getTaskId(), updatedTask.getTitle(), updatedTask.getStatus());

        return new TaskDetailsResponseDto(taskForUpdate);
    }

    private void sendTaskUpdatedEvent(String email, Long taskId, String title, TaskStatus status) {
        var taskIdentifier = new TaskIdentifier(email);
        var event = new TaskUpdatedEvent(taskId, title, status.equals(TaskStatus.DONE) ? true : null);

        kafkaTemplate.send(KafkaConfiguration.TASK_TOPIC_NAME, taskIdentifier, event);
    }

    @Transactional
    public void deleteTask(String username, Long taskId) {
        taskRepository.deleteByTaskIdAndOwnerUsername(taskId, username);
    }

    private void sendTaskDeletedEvent(String email, Long taskId) {
        var taskIdentifier = new TaskIdentifier(email);
        var event = new TaskDeletedEvent(taskId);

        kafkaTemplate.send(KafkaConfiguration.TASK_TOPIC_NAME, taskIdentifier, event);
    }

    private NoSuchEntityException taskDoesntExistException(Long id, String username) {
        return new NoSuchEntityException("Task with ID=%s doesn't exist for %s".formatted(id, username));
    }
}
