package com.arsnyan.account.model.message;

public record EmailTask(
        String to,
        String subject,
        String htmlBody
) {}
