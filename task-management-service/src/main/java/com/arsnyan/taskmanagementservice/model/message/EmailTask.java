package com.arsnyan.taskmanagementservice.model.message;

public record EmailTask(
        String to,
        String subject,
        String htmlBody
) {}
