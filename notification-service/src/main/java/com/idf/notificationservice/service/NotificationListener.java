package com.idf.notificationservice.service;

import com.idf.notificationservice.dto.response.NotificationMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import reactor.rabbitmq.Receiver;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationListener {

    private final Receiver receiver;
    private final MailService mailService;

    @PostConstruct
    private void init() {
        startListening();
    }

    private void startListening() {
        receiver.consumeAutoAck("notification.queue")
                .flatMap(delivery -> {
                    NotificationMessage msg = deserialize(delivery.getBody());
                    return mailService.sendEmail(msg);
                })
                .subscribe();
    }

    private NotificationMessage deserialize(byte[] body) {
        String text = new String(body);

        String email = "test@mail.com";
        String subject = "Notification of poor meme quality";

        return new NotificationMessage(email, subject, text);
    }
}
