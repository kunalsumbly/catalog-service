package com.polarbookshop.catalogservice.config.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class LoggerDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(LoggerDelegate.class);

    private Environment environment;
    private RestTemplate restTemplate;

    public LoggerDelegate(Environment environment) {
        this.environment = environment;
        this.restTemplate = new RestTemplate();
    }

    public static final String LOGGING_ACTUATOR_URL = "http://localhost:8080/actuator/loggers/root";


    public void handleLogLevelUpdates(String key, HttpHeaders headers) {
        try{
            LOG.info("Detected changes for logging.level, updating log level");
            String newLogLevel = environment.getProperty(key);
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
        } catch (Exception e) {
            LOG.error("Error occurred while updating log level for key: {}", key, e);
        }
    }

}
