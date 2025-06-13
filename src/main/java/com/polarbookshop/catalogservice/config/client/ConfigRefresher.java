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


@Component
public class ConfigRefresher {


    private Environment environment;

    public ConfigRefresher(Environment environment) {
        this.environment = environment;
    }
    private static final Logger LOG = LoggerFactory.getLogger(ConfigRefresher.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String REFRESH_URL = "http://localhost:8080/actuator/refresh";
    public static final String LOGGING_ACTUATOR_URL = "http://localhost:8080/actuator/loggers/root";

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
           if (refreshedKeys != null && refreshedKeys.contains("logging.level.root")) {
               LOG.info("Detected changes for logging.level.root, updating log level");
               String newLogLevel = environment.getProperty("logging.level.root");
               LOG.info("New log level: {}", newLogLevel);

               // Prepare payload
               Map<String, String> logPayload = new HashMap<>();
               logPayload.put("configuredLevel", newLogLevel);

               HttpEntity<Map<String, String>> logRequest = new HttpEntity<>(logPayload, headers);

               // Call /actuator/loggers/root
               ResponseEntity<String> logResponse = restTemplate.exchange(
                       LOGGING_ACTUATOR_URL,
                       HttpMethod.POST,
                       logRequest,
                       String.class
               );
               LOG.info("Log level updated. Response: {}", logResponse.getStatusCode());
           }

        } catch (Exception e) {
            // Log error details
            LOG.warn("Failed to refresh configuration:{)", e.getMessage());
        }
    }
}
