#!/bin/bash
#
# Usage:
#   - Run without arguments to build the application JAR only:
#     ./local_build.sh
#
#   - Run with BUILD_DOCKER_IMAGE=true to build both JAR and Docker image:
#     BUILD_DOCKER_IMAGE=true ./local_build.sh
#
#   - When run from Dockerfile, set DOCKER_BUILD=true to skip Docker operations:
#     This is handled automatically in the Dockerfile
#

set -e

# Check if we're running in Docker build context
if [ -z "$DOCKER_BUILD" ]; then
    # Running locally, use SDKMAN
    export SDKMAN_DIR="$HOME/.sdkman"
    [ -s "$SDKMAN_DIR/bin/sdkman-init.sh" ] && source "$SDKMAN_DIR/bin/sdkman-init.sh"
fi

echo "loading the env vars and exporting them"
# load the .env file variable here
SCRIPT_DIR=$(dirname "$0")
set -a
[ -f "$SCRIPT_DIR/.env" ] && . "$SCRIPT_DIR/.env"
set +a

CATALOG_SERVICE="catalog-service"

echo "building catalog service jar"
# build catalog service using ./gradlew clean assemble

# Check if we're running in Docker build context
if [ -z "$DOCKER_BUILD" ]; then
    # Running locally, change directory and use sdk
    cd ../catalog-service
    sdk env
else
    # Running in Docker build, stay in current directory
    echo "Running in Docker build context"
fi

./gradlew clean assemble

# Check if we're running in Docker build context
if [ -z "$DOCKER_BUILD" ]; then
    # Check if BUILD_DOCKER_IMAGE is set to true
    if [ "$BUILD_DOCKER_IMAGE" = "true" ]; then
        # Running locally, continue with Docker operations
        TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")

        # build the docker image now
        echo "Now building the docker image for catalog service"

        docker build --platform=linux/amd64 -t "${CATALOG_SERVICE}:${TIMESTAMP}" .

        echo "building config service jar"

        docker container stop catalog-service || true
        docker container rm catalog-service || true
    else
        echo "Skipping Docker image build. Set BUILD_DOCKER_IMAGE=true to build Docker image."
    fi
fi
