# Use a lightweight JDK base image
FROM azul/zulu-openjdk:17-latest

# Create an app directory inside the container
WORKDIR /app

# Copy the jar file into /app
# Replace 'your-app.jar' with your actual jar name
COPY build/libs/catalog-service-0.0.1-SNAPSHOT.jar /app/catalog-service.jar

# Expose port 8080 (for documentation only)
EXPOSE 8080

# Command to run the app
ENTRYPOINT ["java", "-jar", "/app/catalog-service.jar"]
