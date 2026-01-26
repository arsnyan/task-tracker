package com.arsnyan.taskmanagementservice.listener;

import com.arsnyan.taskmanagementservice.model.message.UserEvent;
import com.arsnyan.taskmanagementservice.service.UserEventService;
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
public class UserEventsListener {
    private final UserEventService userEventService;

    @KafkaListener(
            topics = "${app.kafka.topics.user_events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onUserEvent(
            @Payload UserEvent userEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Received even: type={}, userId={}, topic={}, partition={}, offset={}",
                userEvent.type(), userEvent.userId(), topic, partition, offset);

        userEventService.process(userEvent);
    }
}
