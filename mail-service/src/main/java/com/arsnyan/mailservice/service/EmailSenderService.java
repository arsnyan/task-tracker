package com.arsnyan.mailservice.service;

import com.arsnyan.mailservice.model.message.EmailTask;
import com.arsnyan.mailservice.repository.EmailLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {
    private final JavaMailSender mailSender;
    private final EmailLimiter emailLimiter;

    public void process(EmailTask emailTask) {
        if (emailLimiter.reserveLimit()) {
            var message = new SimpleMailMessage();
            message.setFrom("noreply@arsnyan.com");
            message.setTo(emailTask.to());
            message.setSubject(emailTask.subject());
            message.setText(emailTask.htmlBody());
            mailSender.send(message);
        } else {
            log.warn("Message to {} with subject {} couldn't be sent, email has been limit reached.", emailTask.to(), emailTask.subject());
        }
    }
}
