package com.arsnyan.account.service;

import com.arsnyan.account.config.KafkaConfiguration;
import com.arsnyan.account.dto.RegisterRequestDto;
import com.arsnyan.account.model.message.EmailTask;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailPreparationService {
    public static final String WELCOME_EMAIL_SUBJECT = "Welcome to task tracker";

    private final KafkaTemplate<String, EmailTask> kafkaTemplate;

    @Value("${app.email.welcome-user-registered-path}")
    private Resource welcomeEmailBodyTemplateResource;
    private String welcomeEmailBodyTemplate;

    @PostConstruct
    public void init() throws IOException {
        welcomeEmailBodyTemplate = StreamUtils.copyToString(welcomeEmailBodyTemplateResource.getInputStream(), StandardCharsets.UTF_8);
    }

    public void sendMessage(RegisterRequestDto registerRequest) {
        var username = registerRequest.username();
        var email = registerRequest.email();

        var emailTask = new EmailTask(email, WELCOME_EMAIL_SUBJECT, welcomeEmailBodyTemplate.formatted(username));

        kafkaTemplate.send(KafkaConfiguration.EMAIL_SENDING_TASKS_TOPIC_NAME, email, emailTask)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error(ex.getMessage(), ex);
                    } else {
                        log.info("Sent a message to Kafka topic {} for a welcome email",
                                KafkaConfiguration.EMAIL_SENDING_TASKS_TOPIC_NAME);
                    }
                });
    }
}
