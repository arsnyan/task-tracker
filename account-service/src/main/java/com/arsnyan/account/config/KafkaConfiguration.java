package com.arsnyan.account.config;

import com.arsnyan.account.model.EmailTask;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfiguration {
    public static final String EMAIL_SENDING_TASKS_TOPIC_NAME = "EMAIL_SENDING_TASKS";
    @Bean
    public NewTopic emailSendingTasksTopic() {
        return TopicBuilder.name(EMAIL_SENDING_TASKS_TOPIC_NAME)
                .partitions(10)
                .replicas(3)
                .build();
    }
}
