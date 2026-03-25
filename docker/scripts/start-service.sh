#!/bin/bash
#
# DocMan Service Start Script
# Runs a single specified JAR
#

set -e

SERVICE_JAR=${SERVICE_JAR:-}
SERVER_PORT=${SERVER_PORT:-8080}

if [ -z "$SERVICE_JAR" ]; then
    echo "ERROR: SERVICE_JAR environment variable is required"
    exit 1
fi

JAR_FILE="/app/${SERVICE_JAR}"

if [ ! -f "$JAR_FILE" ]; then
    echo "ERROR: JAR file not found: $JAR_FILE"
    exit 1
fi

echo "Starting ${SERVICE_JAR} on port ${SERVER_PORT}..."

exec java -jar "$JAR_FILE" --server.port=${SERVER_PORT} "$@"
