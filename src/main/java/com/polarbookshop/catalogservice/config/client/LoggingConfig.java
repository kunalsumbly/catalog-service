package com.polarbookshop.catalogservice.config.client;

import org.apache.commons.logging.Log;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;

/**
 * Configuration class that provides a DeferredLogFactory bean.
 * This is required for the MonitoringConfigDataLoader to properly initialize
 * with logging capabilities during the early bootstrap phase.
 * 
 * Note: While Spring Boot does provide DeferredLogFactory internally, it's not
 * exposed as a bean by default. This class is necessary because:
 * 1. ConfigServerConfigDataLoader requires a DeferredLogFactory in its constructor
 * 2. Our MonitoringConfigDataLoader extends ConfigServerConfigDataLoader
 * 3. We need to provide this bean explicitly for dependency injection to work
 */
@Configuration
public class LoggingConfig {

    /**
     * Creates a DeferredLogFactory bean that will be used by the MonitoringConfigDataLoader.
     * The DeferredLogFactory allows logging to be deferred until the logging system is fully initialized.
     *
     * @return a DeferredLogFactory instance
     */
    @Bean
    public DeferredLogFactory deferredLogFactory() {
        return destination -> destination.get();
    }
}
