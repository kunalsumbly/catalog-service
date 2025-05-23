# Use a lightweight JDK base image
FROM gradle:8.8-jdk21 AS builder

WORKDIR /app

COPY local_build_deploy.sh /app/local_build_deploy.sh

# Copy the Gradle wrapper and source files
COPY . /app


RUN ls -la /app

RUN sleep 99999

# Run the local_build_deploy.sh script that triggers Gradle assemble
RUN chmod +x /app/local_build_deploy.sh && ./app/local_build_deploy.sh



FROM eclipse-temurin:21-jre

# Create an app directory inside the container
WORKDIR /app

# Copy the jar file into /app
# Replace 'your-app.jar' with your actual jar name
COPY --from=builder /app/build/libs/catalog-service-0.0.1-SNAPSHOT.jar /app/catalog-service.jar

# Expose port 8080 (for documentation only)
EXPOSE 8080

# Command to run the app
ENTRYPOINT ["java", "-jar", "/app/catalog-service.jar"]
