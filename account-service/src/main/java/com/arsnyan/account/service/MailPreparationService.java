package com.arsnyan.account.service;

import com.arsnyan.account.config.KafkaConfiguration;
import com.arsnyan.account.dto.RegisterRequestDto;
import com.arsnyan.account.model.message.EmailTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailPreparationService {
    public static final String WELCOME_EMAIL_SUBJECT = "Welcome to task tracker";
    public static final String WELCOME_EMAIL_BODY_TEMPLATE = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0;padding:0;font-family:Arial,sans-serif;background:#f4f4f4;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="padding:40px 20px;">
                    <tr>
                        <td align="center">
                            <table width="480" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:8px;padding:32px;">
                                <tr>
                                    <td align="center" style="padding-bottom:24px;">
                                        <h1 style="margin:0;color:#333;font-size:24px;">Welcome, %s!</h1>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="color:#555;font-size:15px;line-height:1.6;">
                                        <p style="margin:0 0 16px;">🎉 Your account has been created successfully</p>
                                    </td>
                                </tr>
                            </table>
                            <p style="color:#999;font-size:12px;margin-top:24px;">© Arsen's Task Tracker</p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """;

    private final KafkaTemplate<String, EmailTask> kafkaTemplate;

    public void sendMessage(RegisterRequestDto registerRequest) {
        var username = registerRequest.username();
        var email = registerRequest.email();

        var emailTask = new EmailTask(email, WELCOME_EMAIL_SUBJECT, WELCOME_EMAIL_BODY_TEMPLATE.formatted(username));

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
