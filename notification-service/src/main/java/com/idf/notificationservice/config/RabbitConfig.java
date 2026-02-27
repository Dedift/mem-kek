package com.idf.notificationservice.config;

import com.rabbitmq.client.ConnectionFactory;
import reactor.rabbitmq.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public ReceiverOptions receiverOptions() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");

        return new ReceiverOptions()
                .connectionFactory(factory);
    }

    @Bean
    public Receiver receiver(ReceiverOptions options) {
        return RabbitFlux.createReceiver(options);
    }
}
