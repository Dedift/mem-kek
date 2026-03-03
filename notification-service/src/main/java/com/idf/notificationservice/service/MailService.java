package com.idf.notificationservice.service;

import com.idf.notificationservice.dto.response.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public Mono<Void> sendEmail(NotificationMessage msg) {
        return Mono.fromRunnable(() -> {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom("noreply@notification.com");
                    message.setTo(msg.getEmail());
                    message.setSubject(msg.getSubject());
                    message.setText(msg.getBody());
                    mailSender.send(message);
                }).subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}