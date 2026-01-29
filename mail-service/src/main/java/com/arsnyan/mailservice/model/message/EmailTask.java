package com.arsnyan.mailservice.model.message;

public record EmailTask(
        String to,
        String subject,
        String htmlBody
) {}
