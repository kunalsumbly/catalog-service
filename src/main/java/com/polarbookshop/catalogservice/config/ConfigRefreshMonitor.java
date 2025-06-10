package com.polarbookshop.catalogservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Monitors the status of configuration refresh operations.
 * This class is thread-safe and provides methods to track whether
 * the last config refresh attempt was successful.
 */
@Component
@Slf4j
public class ConfigRefreshMonitor {

    // Using AtomicBoolean for thread safety
    private final AtomicBoolean lastRefreshSuccessful = new AtomicBoolean(true);
    private final AtomicBoolean configServerReachable = new AtomicBoolean(true);
    private final AtomicBoolean validEnvironmentReceived = new AtomicBoolean(true);

    /**
     * Records a successful config refresh operation.
     */
    public void markRefreshSuccess() {
        log.debug("Marking config refresh as successful");
        lastRefreshSuccessful.set(true);
    }

    /**
     * Records a failed config refresh operation.
     */
    public void markRefreshFailure() {
        log.warn("Marking config refresh as failed");
        lastRefreshSuccessful.set(false);
    }

    /**
     * Records whether the config server was reachable.
     * 
     * @param reachable true if the config server was reachable, false otherwise
     */
    public void setConfigServerReachable(boolean reachable) {
        if (!reachable) {
            log.warn("Config server was not reachable");
        }
        configServerReachable.set(reachable);
    }

    /**
     * Records whether a valid environment was received from the config server.
     * 
     * @param valid true if a valid environment was received, false otherwise
     */
    public void setValidEnvironmentReceived(boolean valid) {
        if (!valid) {
            log.warn("Invalid or empty environment received from config server");
        }
        validEnvironmentReceived.set(valid);
    }

    /**
     * Checks if the last refresh operation was successful.
     * 
     * @return true if the last refresh was successful, false otherwise
     */
    public boolean wasLastRefreshSuccessful() {
        return lastRefreshSuccessful.get();
    }

    /**
     * Checks if the config server was reachable during the last operation.
     * 
     * @return true if the config server was reachable, false otherwise
     */
    public boolean isConfigServerReachable() {
        return configServerReachable.get();
    }

    /**
     * Checks if a valid environment was received during the last operation.
     * 
     * @return true if a valid environment was received, false otherwise
     */
    public boolean isValidEnvironmentReceived() {
        return validEnvironmentReceived.get();
    }

    /**
     * Checks if all conditions for a successful refresh are met.
     * 
     * @return true if all conditions are met, false otherwise
     */
    public boolean isRefreshSuccessful() {
        boolean successful = isConfigServerReachable() && isValidEnvironmentReceived() && wasLastRefreshSuccessful();
        log.debug("Refresh status: configServerReachable={}, validEnvironmentReceived={}, lastRefreshSuccessful={}, overall={}",
                isConfigServerReachable(), isValidEnvironmentReceived(), wasLastRefreshSuccessful(), successful);
        return successful;
    }

    /**
     * Resets all status flags to their default values.
     */
    public void reset() {
        log.debug("Resetting config refresh monitor state");
        lastRefreshSuccessful.set(true);
        configServerReachable.set(true);
        validEnvironmentReceived.set(true);
    }
}