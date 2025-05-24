# Multi-stage build

# Build stage
FROM eclipse-temurin:21-jdk AS builder

# Install required tools
RUN apt-get update && \
    apt-get install -y curl zip unzip wget

# Create app directory
WORKDIR /app

# Copy source code and build script
COPY . .

# Set DOCKER_BUILD environment variable to true
ENV DOCKER_BUILD=true


RUN keytool -import -trustcacerts -noprompt \
       -alias zscaler \
       -file ZscalerRootCA.pem \
       -keystore /opt/java/openjdk/lib/security/cacerts \
       -storepass changeit

# Make the build script executable and run Gradle directly
RUN chmod +x ./gradlew && \
    ./gradlew clean assemble \
    -Dorg.gradle.jvmargs="-Dhttp.proxyHost=host.docker.internal -Dhttp.proxyPort=3129 -Dhttps.proxyHost=host.docker.internal -Dhttps.proxyPort=3129"

# Runtime stage
FROM amazoncorretto:21.0.4

# Create an app directory inside the container
WORKDIR /app

# Copy the jar file from the builder stage
COPY --from=builder /app/build/libs/catalog-service-0.0.1-SNAPSHOT.jar /app/catalog-service.jar

# Expose port 8080 (for documentation only)
EXPOSE 8080

# Command to run the app
ENTRYPOINT ["java", "-jar", "/app/catalog-service.jar"]
