package com.n11bootcamp.jwtornek.consumer;

import com.n11bootcamp.jwtornek.config.RabbitMQConfig;
import com.n11bootcamp.jwtornek.event.UserRegisteredEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class UserEventConsumer {

    //@RabbitListener(queues = RabbitMQConfig.QUEUE) -- farklı mikroservise taşındı(notification-service)
    public void consume(UserRegisteredEvent event) {

        System.out.println("RabbitMQ EVENT ALINDI");
        System.out.println("Kullanıcı: " + event.getUsername());
        System.out.println("Email: " + event.getEmail());
        System.out.println("Welcome mail sent ");
    }
}