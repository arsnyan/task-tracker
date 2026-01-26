package com.arsnyan.taskmanagementservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProperties(
        Topics topics,
        Consumer consumer
) {
    record Topics(String userEvenets) {}

    record Consumer(
            int concurrency,
            Retry retry
    ) {
        record Retry(int maxAttempts, long backoffMs) {}
    }
}