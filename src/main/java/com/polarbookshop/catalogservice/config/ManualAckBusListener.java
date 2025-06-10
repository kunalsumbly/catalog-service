package com.polarbookshop.catalogservice.config;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * A message listener that manually acknowledges messages from the Spring Cloud Bus.
 * It only acknowledges messages if the config refresh was successful.
 */
@Component
@Slf4j
public class ManualAckBusListener implements ChannelAwareMessageListener {

    private final ContextRefresher contextRefresher;
    private final ConfigRefreshMonitor configRefreshMonitor;

    /**
     * Constructor with required dependencies.
     * Uses @Qualifier to specify which ContextRefresher bean to use.
     * 
     * @param contextRefresher The context refresher to use for refreshing the application context
     * @param configRefreshMonitor The monitor to track refresh status
     */
    public ManualAckBusListener(
            @Qualifier("configDataContextRefresher") ContextRefresher contextRefresher,
            ConfigRefreshMonitor configRefreshMonitor) {
        this.contextRefresher = contextRefresher;
        this.configRefreshMonitor = configRefreshMonitor;
    }

    /**
     * Handles messages from the Spring Cloud Bus.
     * It triggers a context refresh and only acknowledges the message if the refresh was successful.
     *
     * @param message The message from RabbitMQ
     * @param channel The RabbitMQ channel
     * @throws Exception If an error occurs during message processing
     */
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            log.debug("Received message from Spring Cloud Bus: {}", message);

            // Check if this is a message we should process
            // Only process messages with specific headers or properties
            // This helps filter out heartbeat or other system messages
            if (!isConfigRefreshMessage(message)) {
                log.debug("Ignoring non-config-refresh message, acknowledging without processing");
                channel.basicAck(deliveryTag, false);
                return;
            }

            // Reset the monitor state before processing the message
            configRefreshMonitor.reset();

            // We don't need to publish an event, as we're directly handling the message here
            // Instead, we'll directly call contextRefresher.refresh()

            // Perform the actual refresh
            Set<String> keys = contextRefresher.refresh();
            log.debug("Refreshed configuration keys: {}", keys);

            // Mark the refresh as successful if keys were refreshed
            if (keys != null && !keys.isEmpty()) {
                configRefreshMonitor.markRefreshSuccess();
            } else {
                configRefreshMonitor.markRefreshFailure();
                log.warn("No configuration keys were refreshed");
            }

            // Check if the refresh was successful overall
            if (configRefreshMonitor.isRefreshSuccessful()) {
                // ACK the message if the refresh was successful
                log.debug("Config refresh was successful, acknowledging message");
                channel.basicAck(deliveryTag, false);
            } else {
                // NACK and requeue the message if the refresh failed
                log.warn("Config refresh failed, rejecting message and requeueing");
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (Exception e) {
            log.error("Error processing message from Spring Cloud Bus", e);
            configRefreshMonitor.markRefreshFailure();

            try {
                // NACK and requeue the message if an error occurred
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                log.error("Failed to nack message", ioException);
                throw ioException;
            }
        }
    }

    /**
     * Determines if a message is a config refresh message that should be processed.
     * This helps filter out heartbeat or other system messages that might be on the bus.
     *
     * @param message The message to check
     * @return true if the message is a config refresh message, false otherwise
     */
    private boolean isConfigRefreshMessage(Message message) {
        // Get the message headers
        org.springframework.amqp.core.MessageProperties props = message.getMessageProperties();

        // Log all headers for debugging
        log.debug("Message headers: {}", props.getHeaders());

        // Check for Spring Cloud Bus specific headers
        // The type header should contain "RefreshRemoteApplicationEvent" for config refresh events
        String type = props.getHeader("type");
        if (type != null && type.contains("RefreshRemoteApplicationEvent")) {
            return true;
        }

        // Check the content type
        String contentType = props.getContentType();
        if (contentType != null && contentType.contains("RefreshRemoteApplicationEvent")) {
            return true;
        }

        // Check if the message body contains any indicators of a config refresh event
        try {
            String body = new String(message.getBody(), "UTF-8");
            log.debug("Message body: {}", body);
            if (body.contains("RefreshRemoteApplicationEvent") || 
                body.contains("refresh") || 
                body.contains("config")) {
                return true;
            }
        } catch (Exception e) {
            log.warn("Failed to parse message body", e);
        }

        // If we can't find any indicators, default to NOT processing the message
        // This helps filter out unrelated messages
        log.debug("Message does not appear to be a config refresh event, ignoring");
        return false;
    }
}
