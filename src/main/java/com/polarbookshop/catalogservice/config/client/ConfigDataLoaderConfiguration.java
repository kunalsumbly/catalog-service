package com.polarbookshop.catalogservice.config.client;

import com.polarbookshop.catalogservice.config.ConfigRefreshMonitor;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.cloud.config.client.ConfigServerConfigDataLoader;
import org.springframework.cloud.config.client.ConfigServerConfigDataResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

/**
 * Configuration class that registers the custom MonitoringConfigDataLoader
 * to override the default ConfigServerConfigDataLoader.
 */
@Configuration
public class ConfigDataLoaderConfiguration {

    /**
     * Creates a MonitoringConfigDataLoader bean that will be used instead of
     * the default ConfigServerConfigDataLoader.
     * 
     * The @Primary annotation ensures this bean is used when multiple beans
     * of the same type are available.
     *
     * @param environment The Spring environment
     * @param logFactory The DeferredLogFactory for logging
     * @param configRefreshMonitor The monitor to track config refresh status
     * @return A ConfigDataLoader for ConfigServerConfigDataResource
     */
    @Bean
    @Primary
    public ConfigDataLoader<ConfigServerConfigDataResource> configServerConfigDataLoader(
            Environment environment,
            DeferredLogFactory logFactory,
            ConfigRefreshMonitor configRefreshMonitor) {
        
        return new MonitoringConfigDataLoader(logFactory, configRefreshMonitor);
    }
}