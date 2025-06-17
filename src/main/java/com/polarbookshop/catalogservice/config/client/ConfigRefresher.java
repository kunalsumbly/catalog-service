package com.polarbookshop.catalogservice.config.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


@Component
public class ConfigRefresher {

    private LoggerDelegate loggerDelegate;

    public ConfigRefresher (LoggerDelegate loggerDelegate) {
        this.loggerDelegate = loggerDelegate;
    }

    private static final Logger LOG = LoggerFactory.getLogger(ConfigRefresher.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String REFRESH_URL = "http://localhost:8080/actuator/refresh";

    @Scheduled(fixedRate = 60000) // 1 minute (in milliseconds)
    public void refreshConfig() {
        try {
            // Set up headers if needed
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");

            // Create the HTTP entity (header + body)
            HttpEntity<String> requestEntity = new HttpEntity<>("", headers);

            // Execute POST request
            ResponseEntity<List> response = restTemplate.exchange(
                    REFRESH_URL,       // Target URL
                    HttpMethod.POST,   // HTTP method
                    requestEntity,     // Request entity (headers + body)
                    List.class       // Response type
            );

            List<String> refreshedKeys = response.getBody();
            // Log confirmation
            LOG.info("Configuration refreshed successfully, refreshKeys = {}",refreshedKeys);
            refreshedKeys.stream().filter(key -> key.contains("logging.level")).forEach(
                   key ->  loggerDelegate.handleLogLevelUpdates(key,headers)
            );
        } catch (Exception e) {
            // Log error details
            LOG.error("Failed to refresh configuration:{}", e.getMessage());
        }
    }

}
