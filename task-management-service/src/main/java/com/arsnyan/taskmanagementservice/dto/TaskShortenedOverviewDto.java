package com.arsnyan.taskmanagementservice.dto;

import java.time.ZonedDateTime;

public record TaskShortenedOverviewDto(
        String title,
        ZonedDateTime finishedAt
) {}
