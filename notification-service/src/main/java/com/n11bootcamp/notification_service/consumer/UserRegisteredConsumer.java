package com.n11bootcamp.notification_service.consumer;

import com.n11bootcamp.notification_service.config.RabbitMQConfig;
import com.n11bootcamp.notification_service.event.UserRegisteredEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class UserRegisteredConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consume(UserRegisteredEvent event) {

        System.out.println("Notification service event aldı");
        System.out.println("Welcome mail sent to: " + event.getEmail());
        System.out.println("Username: " + event.getUsername());
    }
}
