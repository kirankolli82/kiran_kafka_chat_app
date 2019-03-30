package com.kiran;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Kiran Kolli on 28-03-2019.
 */
@Configuration
public class AppConfig {

    private static Logger log = LoggerFactory.getLogger(AppConfig.class);
    @Bean
    public TransportLaneFactory transportLaneFactory() {
        return id -> new TransportLane() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public CompletableFuture<Void> sendOnLane(String message) {
                log.info("Sent message: {} for id: {}", message, id);
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public String subscribeToMessages(Subscriber subscriber) {
                log.info("Subscription received");
                return "";
            }

            @Override
            public void unsubscribe(String subscriberId) {
                log.info("Unsubscribe request received");
            }
        };
    }
}
