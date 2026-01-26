package com.arsnyan.account.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfiguration {
    public static final String EMAIL_SENDING_TASKS_TOPIC_NAME = "EMAIL_SENDING_TASKS";
    public static final String USER_EVENTS_TOPIC_NAME = "USER_EVENTS";

    @Bean
    public NewTopic emailSendingTasksTopic() {
        return TopicBuilder.name(EMAIL_SENDING_TASKS_TOPIC_NAME)
                .partitions(10)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name(USER_EVENTS_TOPIC_NAME)
                .partitions(10)
                .replicas(3)
                .build();
    }
}
