package com.idf.notificationservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.rabbitmq.ReceiverOptions;

@Configuration
@RequiredArgsConstructor
public class RabbitQueueConfig {

    private final ReceiverOptions receiverOptions;

    @Bean
    public void declareQueue() throws Exception {

        try (com.rabbitmq.client.Connection connection = receiverOptions.getConnectionFactory().newConnection();
             com.rabbitmq.client.Channel channel = connection.createChannel()) {

            channel.queueDeclare("notification.queue", true, false, false, null);
        }
    }
}
