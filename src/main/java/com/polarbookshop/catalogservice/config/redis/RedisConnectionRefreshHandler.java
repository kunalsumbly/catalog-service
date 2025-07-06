package com.polarbookshop.catalogservice.config.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class RedisConnectionRefreshHandler {
    
    private final LettuceConnectionFactory lettuceConnectionFactory;
    
    public RedisConnectionRefreshHandler(@Qualifier("customRedisConnectionFactory") LettuceConnectionFactory lettuceConnectionFactory) {
        this.lettuceConnectionFactory = lettuceConnectionFactory;
    }

    @EventListener
    public void handleRefreshEvent(RefreshEvent event) {
        // Get the keys from the event source
        @SuppressWarnings("unchecked")
        List<String> keys = (List<String>) event.getSource();

        // Check if Redis password was updated
        if (keys.contains("spring.data.redis.password")) {
            log.info("Redis password change detected, resetting connections...");
            resetRedisConnections();
        }
    }
    
    private void resetRedisConnections() {
        try {
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
