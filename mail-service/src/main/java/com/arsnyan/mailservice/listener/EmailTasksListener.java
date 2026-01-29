package com.arsnyan.mailservice.listener;

import com.arsnyan.mailservice.message.EmailTask;
import com.arsnyan.mailservice.service.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailTasksListener {
    private final EmailSenderService emailSenderService;

    @KafkaListener(
            topics = "${app.kafka.topics.email_sending_tasks}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onEmailTask(
            @Payload EmailTask emailTask,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Received task: to={}, subject={}, topic={}, partition={}, offset={}",
                emailTask.to(), emailTask.subject(), topic, partition, offset);

        emailSenderService.process(emailTask);
    }
}
