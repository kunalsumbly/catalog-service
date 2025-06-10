# Spring Cloud Config Refresh Process with Manual Acknowledgment

This document explains the configuration refresh process implemented in this application, which uses Spring Cloud Config with Spring Cloud Bus over RabbitMQ, featuring manual acknowledgment for reliability.

## Components Overview

### 1. ConfigRefreshMonitor

**Purpose**: Tracks the status of configuration refresh operations.

**Key Features**:
- Thread-safe using AtomicBoolean for state tracking
- Monitors three critical aspects of the refresh process:
  - Whether the config server was reachable
  - Whether a valid environment was received
  - Whether the actual refresh operation succeeded
- Provides a unified `isRefreshSuccessful()` method that checks all conditions

**Location**: `com.polarbookshop.catalogservice.config.ConfigRefreshMonitor`

### 2. MonitoringConfigDataLoader

**Purpose**: Intercepts the config loading process to monitor its success or failure.

**Key Features**:
- Extends Spring's `ConfigServerConfigDataLoader`
- Hooks into the config loading process to track:
  - If the config server was reachable
  - If a valid environment was received
- Updates the `ConfigRefreshMonitor` with this information

**Location**: `com.polarbookshop.catalogservice.config.client.MonitoringConfigDataLoader`

### 3. ConfigDataLoaderConfiguration

**Purpose**: Registers the custom `MonitoringConfigDataLoader` to override Spring's default loader.

**Key Features**:
- Uses `@Configuration` and `@Bean` annotations to register the custom loader
- Marks the bean as `@Primary` to ensure it's used instead of the default

**Location**: `com.polarbookshop.catalogservice.config.client.ConfigDataLoaderConfiguration`

### 4. LoggingConfig

**Purpose**: Provides a `DeferredLogFactory` bean required by the `MonitoringConfigDataLoader`.

**Key Features**:
- Creates a bean of `DeferredLogFactory` that's needed for the `ConfigServerConfigDataLoader` constructor
- Enables proper logging during the early bootstrap phase of the application
- Required because Spring Boot doesn't expose `DeferredLogFactory` as a bean by default

**Location**: `com.polarbookshop.catalogservice.config.client.LoggingConfig`

### 5. ManualAckBusListener

**Purpose**: Listens for config refresh events from Spring Cloud Bus and manually acknowledges them.

**Key Features**:
- Implements `ChannelAwareMessageListener` to handle RabbitMQ messages
- Filters out non-config-refresh messages to prevent unnecessary processing
- Uses `ContextRefresher` to perform the actual refresh
- Only acknowledges messages if:
  - The message is identified as a config refresh event
  - The config server was reachable
  - A valid environment was received
  - The refresh operation succeeded
- Otherwise, it negatively acknowledges (NACKs) the message for requeuing

**Location**: `com.polarbookshop.catalogservice.config.ManualAckBusListener`

### 6. RabbitMQListenerConfig

**Purpose**: Configures RabbitMQ for Spring Cloud Bus with manual acknowledgment.

**Key Features**:
- Creates a durable named queue for config refresh messages
- Sets up the Spring Cloud Bus exchange
- Binds the queue to the exchange
- Configures a message listener container with manual acknowledgment mode

**Location**: `com.polarbookshop.catalogservice.config.RabbitMQListenerConfig`

## The Config Refresh Process

### 1. Initial Configuration Loading

When the application starts:
1. Spring Boot's config loading mechanism is triggered
2. Our `MonitoringConfigDataLoader` intercepts the loading process
3. It attempts to fetch configuration from the config server
4. It updates `ConfigRefreshMonitor` with the status:
   - Was the config server reachable?
   - Was a valid environment received?

### 2. Runtime Configuration Refresh

When a configuration change is published:
1. The change is pushed to the config server
2. The config server publishes a message to the Spring Cloud Bus (RabbitMQ)
3. The message is routed to our application's queue
4. `ManualAckBusListener` receives the message and:
   - Checks if the message is a config refresh event by examining headers and content
   - If not a config refresh event: ACKs the message without processing
   - If it is a config refresh event:
     - Resets the `ConfigRefreshMonitor` state
     - Calls `contextRefresher.refresh()` to refresh the application context
     - Updates `ConfigRefreshMonitor` with the refresh result
     - Checks `configRefreshMonitor.isRefreshSuccessful()`
     - If successful: ACKs the message
     - If unsuccessful: NACKs the message for requeuing

### 3. Manual Refresh via Actuator

When a manual refresh is triggered via `/actuator/refresh`:
1. Spring's `RefreshEndpoint` is invoked
2. It calls the same `contextRefresher.refresh()` used by our listener
3. The refresh process updates the application context
4. No RabbitMQ message is involved in this case

## Benefits of This Implementation

1. **Reliability**: Configuration changes are only acknowledged if successfully applied
2. **Resilience**: Failed refreshes are retried automatically via RabbitMQ requeuing
3. **Monitoring**: The refresh process is fully monitored and logged
4. **Thread Safety**: All state tracking is thread-safe using atomic variables
5. **Efficiency**: Intelligent message filtering prevents processing of non-config-refresh messages, reducing unnecessary CPU and network usage

## Configuration

The system is configured in `application.yml` with these key settings:

```yaml
spring:
  cloud:
    bus:
      enabled: true
      destination: configRefreshQueue
  rabbitmq:
    listener:
      simple:
        acknowledge-mode: manual
```

This ensures Spring Cloud Bus is enabled and configured to use manual acknowledgment mode.
