package com.arsnyan.mailservice.message;

public record EmailTask(
        String to,
        String subject,
        String htmlBody
) {}
