package com.polarbookshop.catalogservice.config.redis;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class RedisConnectionRefreshHandler {

    private final ApplicationContext applicationContext;

    public RedisConnectionRefreshHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        log.info("RedisConnectionRefreshHandler initialized successfully");
    }

    @EventListener
    public void handleEnvironmentChangeEvent(EnvironmentChangeEvent event) {
        log.info("=== ENVIRONMENT CHANGE EVENT RECEIVED ===");
        log.info("Changed keys: {}", event.getKeys());

        // EnvironmentChangeEvent DOES have getKeys() method
        if (event.getKeys().contains("spring.data.redis.password")) {
            log.info("Redis password change detected, resetting connections...");
            resetRedisConnections();
        } else {
            log.info("No Redis password change detected");
        }
    }
    
    private void resetRedisConnections() {
        try {
            // Get the fresh @RefreshScope bean from application context
            LettuceConnectionFactory lettuceConnectionFactory = applicationContext.getBean(
                    "customRedisConnectionFactory", LettuceConnectionFactory.class
            );

            // Reset the underlying shared connection
            lettuceConnectionFactory.resetConnection();
            
            // Initialize new connection with updated credentials
            lettuceConnectionFactory.initConnection();
            
            log.info("Redis connections reset successfully after password change");
        } catch (Exception e) {
            log.error("Failed to reset Redis connections", e);
            // Consider adding metrics/alerting here for production monitoring
        }
    }
}
