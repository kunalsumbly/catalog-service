package com.polarbookshop.catalogservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for RabbitMQ listener setup.
 * This class configures the queue, exchange, binding, and listener container
 * for the Spring Cloud Bus messages.
 */
@Configuration
public class RabbitMQListenerConfig {

    @Value("${spring.cloud.bus.destination:configRefreshQueue}")
    private String queueName;

    @Value("${spring.cloud.bus.exchange:springCloudBus}")
    private String exchangeName;

    /**
     * Creates a durable queue for config refresh messages.
     *
     * @return The queue bean
     */
    @Bean
    public Queue configRefreshQueue() {
        // Create a durable queue that will survive broker restarts
        return new Queue(queueName, true);
    }

    /**
     * Creates a topic exchange for Spring Cloud Bus messages.
     *
     * @return The exchange bean
     */
    @Bean
    public TopicExchange springCloudBusExchange() {
        // Create a durable exchange that will survive broker restarts
        return new TopicExchange(exchangeName, true, false);
    }

    /**
     * Creates a binding between the queue and the exchange.
     * The routing key "#" means all messages published to the exchange will be routed to the queue.
     *
     * @param queue The queue to bind
     * @param exchange The exchange to bind to
     * @return The binding bean
     */
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        // Bind the queue to the exchange with routing key "#" (all messages)
        return BindingBuilder.bind(queue).to(exchange).with("#");
    }

    /**
     * Creates a message listener container for the ManualAckBusListener.
     * This container will listen for messages on the configRefreshQueue and
     * delegate to the ManualAckBusListener for processing.
     *
     * @param connectionFactory The RabbitMQ connection factory
     * @param manualAckBusListener The listener that will process the messages
     * @return The listener container bean
     */
    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(
            ConnectionFactory connectionFactory,
            ManualAckBusListener manualAckBusListener) {
        
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(manualAckBusListener);
        
        // Enable manual acknowledgment mode
        container.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        
        // Set concurrency (adjust as needed)
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(1);
        
        return container;
    }
}