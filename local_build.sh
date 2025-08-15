#!/bin/bash

# Parse command line arguments
USE_PROXY=true
for arg in "$@"; do
    case $arg in
        noproxy)
            USE_PROXY=false
            shift
            ;;
        *)
            # Unknown option
            ;;
    esac
done

# Function to check Java version
check_java_version() {
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -lt 17 ]; then
            echo "ERROR: Java 17 or higher is required. Current Java version: $JAVA_VERSION"
            echo "Please install Java 17+ or use SDKMAN to manage Java versions:"
            echo "  curl -s \"https://get.sdkman.io\" | bash"
            echo "  sdk install java 17.0.2-open"
            echo "  sdk use java 17.0.2-open"
            exit 1
        else
            echo "Java version check passed: $JAVA_VERSION"
        fi
    else
        echo "ERROR: Java is not installed or not in PATH"
        exit 1
    fi
}

# Source SDKMAN and set the sdk env which will read .sdkmanrc 
if [ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    echo "Sourcing SDKMAN and setting up SDK environment from .sdkmanrc"
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    sdk env
    echo "SDK environment configured successfully"
else
    echo "SDKMAN not found, checking system Java version"
    check_java_version
fi

# set the gradle clean assemble 
if [ "$USE_PROXY" = true ]; then
    echo "Running Gradle build with proxy settings..."
    ./gradlew clean assemble -Dhttp.proxyHost=localhost -Dhttp.proxyPort=3129 -Dhttps.proxyHost=localhost -Dhttps.proxyPort=3129
else
    echo "Running Gradle build without proxy settings..."
    ./gradlew clean assemble
fi

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful, copying JAR file..."
    # copy the jar from build/libs/ to local_build/
    cp ./build/libs/catalog-service-0.0.1-SNAPSHOT.jar ./local_build/
    if [ $? -eq 0 ]; then
        echo "JAR file successfully copied to ./local_build/"
    else
        echo "ERROR: Failed to copy JAR file"
        exit 1
    fi
else
    echo "ERROR: Build failed"
    exit 1
fi
