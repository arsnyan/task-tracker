package com.arsnyan.account.model;

public record EmailTask(
        String to,
        String subject,
        String htmlBody
) {}
