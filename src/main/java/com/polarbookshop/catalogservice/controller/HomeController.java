package com.polarbookshop.catalogservice.controller;

import com.polarbookshop.catalogservice.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class HomeController {
    private final AppProperties appProperties;
    private final RedisTemplate<String, String> redisTemplate;
    private final ValueOperations<String, String> valueOperations;
    private static final String DISTRIBUTION_LIST_KEY = "employeeDistributionList";
    private static final Duration CACHE_TTL = Duration.ofSeconds(20);

    public HomeController(AppProperties appProperties,
                          @Qualifier("customStringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.appProperties = appProperties;
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
    }

    @GetMapping("/")
    public String getGreeting() {
        log.info("Hit ME info!!! controller called ::::");
        log.warn("Hit ME warn!!! controller called ::::");
        log.debug("Hit ME debug !!! controller called ::::");
        log.error("Hit ME error!!! controller called ::::");
        log.trace("Hit ME trace !!! controller called ::::");
        return appProperties.getProperty();
    }

    @GetMapping("/getDistributionList")
    public String getDistributionList() {
        // First check if the value exists in Redis cache
        String cachedValue = valueOperations.get(DISTRIBUTION_LIST_KEY);

        if (cachedValue != null) {
            log.info("The file was read from cache !!!");
            return cachedValue;
        }

        // If not in cache, read from file
        log.info("Distribution list not found in cache, reading from file");
        String content = readDistributionListFromFile();

        // Store in Redis with TTL
        valueOperations.set(DISTRIBUTION_LIST_KEY, content, CACHE_TTL.getSeconds(), TimeUnit.SECONDS);
        log.info("The file was read successfully and cache populated !!!");

        return content;
    }

    private String readDistributionListFromFile() {
        try {
            ClassPathResource resource = new ClassPathResource("distributionList.json");
            byte[] bytes = resource.getInputStream().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error reading distribution list file", e);
            throw new RuntimeException("Failed to read distribution list", e);
        }
    }
}
