# Testing Dynamic Logging Level Changes

This guide explains how to test the dynamic logging level changes in the catalog-service application.

## Prerequisites

- The catalog-service application is running
- You have access to a terminal or API client (like curl, Postman, etc.)

## Steps to Test

### 1. Start the Application

Start the application with the local profile:

```bash
./gradlew bootRun
```

### 2. Check the Current Logging Level

By default, the application starts with the logging level set to DEBUG (as defined in application.properties).

To verify the current logging level, make a GET request to the loggers endpoint:

```bash
curl http://localhost:8080/actuator/loggers
```

Or for a specific logger:

```bash
curl http://localhost:8080/actuator/loggers/ROOT
```

### 3. Test the Current Logging Behavior

Access the root endpoint to see the current logging behavior:

```bash
curl http://localhost:8080/
```

With the DEBUG level, you should see INFO, DEBUG, ERROR, and WARN logs in the console (but not TRACE logs).

### 4. Change the Logging Level

To change the logging level to ERROR (so only ERROR logs are shown), make a POST request to the loggers endpoint:

```bash
curl -X POST -H "Content-Type: application/json" -d '{"configuredLevel": "ERROR"}' http://localhost:8080/actuator/loggers/ROOT
```

### 5. Verify the Changes

Access the root endpoint again to verify that only ERROR logs are shown:

```bash
curl http://localhost:8080/
```

Now, you should only see the ERROR log in the console:
```
ERROR ::: Hit ME !!! controller called ::::
```

The INFO, DEBUG, WARN, and TRACE logs should no longer appear.

### 6. Change Back to Another Level (Optional)

You can change the logging level back to any other level as needed:

```bash
# Change to INFO level
curl -X POST -H "Content-Type: application/json" -d '{"configuredLevel": "INFO"}' http://localhost:8080/actuator/loggers/ROOT

# Change to WARN level
curl -X POST -H "Content-Type: application/json" -d '{"configuredLevel": "WARN"}' http://localhost:8080/actuator/loggers/ROOT

# Change to DEBUG level
curl -X POST -H "Content-Type: application/json" -d '{"configuredLevel": "DEBUG"}' http://localhost:8080/actuator/loggers/ROOT

# Change to TRACE level
curl -X POST -H "Content-Type: application/json" -d '{"configuredLevel": "TRACE"}' http://localhost:8080/actuator/loggers/ROOT
```

## Notes

- The changes made through the actuator endpoint are temporary and will be reset when the application restarts
- For permanent changes, update the ROOT_LOGGER_LEVEL property in the configuration files
- The config server can also be used to change the logging level remotely for all instances