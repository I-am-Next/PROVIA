package com.pm.userservice.infrastructure.kafka;

import com.pm.contracts.auth.UserRegisteredEvent;
import com.pm.userservice.infrastructure.persistence.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserRegisteredConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(UserRegisteredConsumer.class);

    private final UserProfileRepository repository;

    public UserRegisteredConsumer(
            UserProfileRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    @KafkaListener(
            topics = "user-registered",
            groupId = "user-service"
    )
    public void consume(UserRegisteredEvent event) {

        int inserted = repository.insertIfAbsent(
                event.userId(),
                event.email()
        );

        if (inserted == 0) {
            log.debug(
                    "User profile already exists for authUserId={}",
                    event.userId()
            );
            return;
        }

        log.info(
                "Created user profile for authUserId={}",
                event.userId()
        );
    }
}