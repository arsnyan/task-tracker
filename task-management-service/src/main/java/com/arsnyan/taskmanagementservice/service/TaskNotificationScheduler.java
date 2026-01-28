package com.arsnyan.taskmanagementservice.service;

import com.arsnyan.taskmanagementservice.config.KafkaConfiguration;
import com.arsnyan.taskmanagementservice.dto.TaskShortenedOverviewDto;
import com.arsnyan.taskmanagementservice.model.Task;
import com.arsnyan.taskmanagementservice.model.TaskStatus;
import com.arsnyan.taskmanagementservice.model.User;
import com.arsnyan.taskmanagementservice.model.message.EmailTask;
import com.arsnyan.taskmanagementservice.repository.RedisLock;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskNotificationScheduler {
    private static final String EMAIL_SUBJECT = "Daily task reminder";

    private static final String LOCK_KEY = "scheduler:task-notifications";
    private static final Duration LOCK_TTL = Duration.ofMinutes(10);

    private final RedisLock redisLock;
    private final TaskService taskService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Clock clock;

    // Not the prettiest solution I've built, but... Oh, well.
    @Value("${app.task-reminder.max-tasks}")
    private int maxReminderTasks;

    @Value("${app.email.daily-task-reminder-main-path}")
    private Resource mainTemplateResource;
    private String mainTemplate;

    @Value("${app.email.daily-task-reminder-finished-path}")
    private Resource finishedPlaceholderTemplateResource;
    private String finishedPlaceholderTemplate;

    @Value("${app.email.daily-task-reminder-finished-row-path}")
    private Resource finishedRowTemplateResource;
    private String finishedRowTemplate;

    @Value("${app.email.daily-task-reminder-unfinished-path}")
    private Resource unfinishedPlaceholderTemplateResource;
    private String unfinishedPlaceholderTemplate;

    @Value("${app.email.daily-task-reminder-unfinished-row-path}")
    private Resource unfinishedRowTemplateResource;
    private String unfinishedRowTemplate;

    @Value("${app.email.motivational-quote}")
    private String motivationalQuote;

    @Value("${app.email.cta-url}")
    private String ctaUrl;

    @Value("${app.email.unsubscribe-url}")
    private String unsubscribeUrl;

    @Value("${app.email.preferences-url}")
    private String preferencesUrl;

    @PostConstruct
    public void init() throws IOException {
        mainTemplate = StreamUtils.copyToString(mainTemplateResource.getInputStream(), StandardCharsets.UTF_8);
        finishedPlaceholderTemplate = StreamUtils.copyToString(finishedPlaceholderTemplateResource.getInputStream(), StandardCharsets.UTF_8);
        finishedRowTemplate = StreamUtils.copyToString(finishedRowTemplateResource.getInputStream(), StandardCharsets.UTF_8);
        unfinishedPlaceholderTemplate = StreamUtils.copyToString(unfinishedPlaceholderTemplateResource.getInputStream(), StandardCharsets.UTF_8);
        unfinishedRowTemplate = StreamUtils.copyToString(unfinishedRowTemplateResource.getInputStream(), StandardCharsets.UTF_8);
    }

    @Scheduled(cron = "0 0 23 * * *")
    public void scheduleTaskNotificationMail() {
        // I should've probably used ShedLock for this task,
        // however I wanted to experiment with manual locking and Redis for a bit
        if (!redisLock.tryLock(LOCK_KEY, LOCK_TTL)) {
            log.info("Task already running on another instance. Skipping.");
            return;
        }

        var allTasks = taskService.findAllWithOwners().stream()
                .sorted(Comparator.comparing(Task::getTaskId).reversed())
                .toList();

        if (allTasks.isEmpty()) {
            log.info("No tasks found. Skipping.");
            return;
        }

        try {
            var tasksByUser = allTasks.stream()
                    .collect(Collectors.groupingBy(Task::getOwner));

            tasksByUser.forEach((user, rawTasks) -> {
                var finishedTaskList = rawTasks.parallelStream()
                        .map(task -> {
                            if (task.isFinished()) {
                                return new TaskShortenedOverviewDto(task.getTitle(), task.getFinishedAt());
                            }

                            return null;
                        })
                        .filter(Objects::nonNull)
                        .limit(maxReminderTasks)
                        .toList();

                var unfinishedTasks = rawTasks.parallelStream()
                        .map(task -> {
                            if (Set.of(TaskStatus.CREATED, TaskStatus.IN_BACKLOG).contains(task.getStatus())) {
                                return new TaskShortenedOverviewDto(task.getTitle(), task.getFinishedAt());
                            }

                            return null;
                        })
                        .filter(Objects::nonNull)
                        .limit(maxReminderTasks)
                        .toList();

                sendEmailTemplate(user, finishedTaskList, unfinishedTasks);
            });
        } finally {
            redisLock.release(LOCK_KEY);
        }
    }

    private void sendEmailTemplate(
            User user,
            List<TaskShortenedOverviewDto> finishedTasks,
            List<TaskShortenedOverviewDto> unfinishedTasks
    ) {
        var finishedRows = finishedTasks.stream()
                .map(task -> finishedRowTemplate.formatted(
                        task.title(),
                        formatDateTime(task.finishedAt())
                ))
                .collect(Collectors.joining());

        var unfinishedRows = unfinishedTasks.stream()
                .map(task -> unfinishedRowTemplate.formatted(task.title()))
                .collect(Collectors.joining());

        var finishedSection = finishedPlaceholderTemplate.formatted(finishedTasks.size(), finishedRows);
        var unfinishedSection = unfinishedPlaceholderTemplate.formatted(unfinishedTasks.size(), unfinishedRows);

        var email = mainTemplate.formatted(
                user.getUsername(), formatDateTime(ZonedDateTime.now(clock)), finishedSection, unfinishedSection,
                motivationalQuote, ctaUrl, unsubscribeUrl, preferencesUrl
        );

        var emailTask = new EmailTask(email, EMAIL_SUBJECT, email);

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

    private String formatDateTime(ZonedDateTime localDateTime) {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).format(localDateTime);
    }
}
