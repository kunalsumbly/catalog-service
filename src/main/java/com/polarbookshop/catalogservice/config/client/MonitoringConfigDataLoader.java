package com.polarbookshop.catalogservice.config.client;

import com.polarbookshop.catalogservice.config.ConfigRefreshMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.cloud.config.client.ConfigServerConfigDataLoader;
import org.springframework.cloud.config.client.ConfigServerConfigDataResource;
import org.springframework.util.CollectionUtils;

/**
 * Custom implementation of ConfigServerConfigDataLoader that monitors the config loading process
 * and updates the ConfigRefreshMonitor with the status of the operation.
 */
@Slf4j
public class MonitoringConfigDataLoader extends ConfigServerConfigDataLoader {

    private final ConfigRefreshMonitor configRefreshMonitor;

    /**
     * Constructor that takes a DeferredLogFactory and ConfigRefreshMonitor.
     *
     * @param logFactory           the DeferredLogFactory to use for logging
     * @param configRefreshMonitor the ConfigRefreshMonitor to update with status
     */
    public MonitoringConfigDataLoader(DeferredLogFactory logFactory, ConfigRefreshMonitor configRefreshMonitor) {
        super(logFactory);
        this.configRefreshMonitor = configRefreshMonitor;
        log.debug("MonitoringConfigDataLoader initialized");
    }

    /**
     * Loads configuration data from the config server and monitors the process.
     *
     * @param context  the context for loading
     * @param resource the resource to load
     * @return the loaded ConfigData
     * @throws ConfigDataResourceNotFoundException if the resource cannot be found
     */
    @Override
    public ConfigData load(ConfigDataLoaderContext context, ConfigServerConfigDataResource resource)
            throws ConfigDataResourceNotFoundException {
        log.debug("Loading config data from config server");

        try {
            // Attempt to load the config data from the config server
            ConfigData configData = super.load(context, resource);

            // Mark the config server as reachable
            configRefreshMonitor.setConfigServerReachable(true);

            // Check if the environment is valid
            boolean isValid = configData != null && !CollectionUtils.isEmpty(configData.getPropertySources());
            configRefreshMonitor.setValidEnvironmentReceived(isValid);
            
            if (isValid) {
                log.debug("Valid environment received from config server");
            } else {
                log.warn("Invalid or empty environment received from config server");
            }

            return configData;
        } catch (Exception e) {
            // If an exception occurs, mark the config server as unreachable
            configRefreshMonitor.setConfigServerReachable(false);
            configRefreshMonitor.setValidEnvironmentReceived(false);
            log.error("Failed to load config data from config server", e);
            throw e;
        }
    }
}