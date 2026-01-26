package com.arsnyan.taskmanagementservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskCreateRequestDto(
        @Size(min = 1)
        @NotNull
        String title,

        String content
) {}
