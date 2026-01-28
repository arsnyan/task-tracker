package com.arsnyan.taskmanagementservice.controller;

import com.arsnyan.taskmanagementservice.dto.TaskCreateRequestDto;
import com.arsnyan.taskmanagementservice.dto.TaskDetailsResponseDto;
import com.arsnyan.taskmanagementservice.dto.TaskOverviewResponseDto;
import com.arsnyan.taskmanagementservice.dto.TaskUpdateRequestDto;
import com.arsnyan.taskmanagementservice.service.TaskService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<@NonNull List<TaskOverviewResponseDto>> getTasks(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(taskService.getAllByUsername(jwt.getClaimAsString("username")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDetailsResponseDto> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(taskService.getTaskById(jwt.getClaimAsString("username"), id));
    }

    @PostMapping
    public ResponseEntity<TaskDetailsResponseDto> createTask(
            @RequestBody @Valid TaskCreateRequestDto dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(jwt.getClaimAsString("username"), dto));
    }

    @PostMapping("/{id}")
    public ResponseEntity<TaskDetailsResponseDto> updateTask(
            @RequestBody @Valid TaskUpdateRequestDto dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(taskService.updateTask(jwt.getClaimAsString("username"), dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        taskService.deleteTask(jwt.getClaimAsString("username"), id);
        return  ResponseEntity.noContent().build();
    }
}
