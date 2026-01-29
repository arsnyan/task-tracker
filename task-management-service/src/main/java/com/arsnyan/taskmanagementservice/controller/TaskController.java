package com.arsnyan.taskmanagementservice.controller;

import com.arsnyan.taskmanagementservice.dto.TaskCreateRequestDto;
import com.arsnyan.taskmanagementservice.dto.TaskDetailsResponseDto;
import com.arsnyan.taskmanagementservice.dto.TaskOverviewResponseDto;
import com.arsnyan.taskmanagementservice.dto.TaskUpdateRequestDto;
import com.arsnyan.taskmanagementservice.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tasks", description = "Task management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {
    private final TaskService taskService;

    @Operation(summary = "Get all tasks", description = "Returns all tasks for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ResponseEntity<@NonNull List<TaskOverviewResponseDto>> getTasks(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(taskService.getAllByUsername(jwt.getClaimAsString("username")));
    }

    @Operation(summary = "Get task by ID", description = "Returns a specific task by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskDetailsResponseDto> getTaskById(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(taskService.getTaskById(jwt.getClaimAsString("username"), id));
    }

    @Operation(summary = "Create a new task", description = "Creates a new task for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @PostMapping
    public ResponseEntity<TaskDetailsResponseDto> createTask(
            @RequestBody @Valid TaskCreateRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(jwt.getClaimAsString("username"), dto));
    }

    @Operation(summary = "Update a task", description = "Updates an existing task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PostMapping("/{id}")
    public ResponseEntity<TaskDetailsResponseDto> updateTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @RequestBody @Valid TaskUpdateRequestDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(taskService.updateTask(jwt.getClaimAsString("username"), dto));
    }

    @Operation(summary = "Delete a task", description = "Deletes a task by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt
    ) {
        taskService.deleteTask(jwt.getClaimAsString("username"), id);
        return  ResponseEntity.noContent().build();
    }
}
