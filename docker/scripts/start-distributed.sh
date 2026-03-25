#!/bin/bash
#
# DocMan Distributed Mode Start Script
# Each service runs in its own JVM
#

set -e

echo "============================================"
echo "  DocMan Distributed - Starting Services"
echo "============================================"

# Function to start a service
start_svc() {
    local name="$1"
    local jar="$2"
    local port="$3"
    shift 3
    echo "Starting $name on port $port..."
    java -jar "$jar" --server.port="$port" "$@" &
}

# Start Gateway
start_svc "Gateway" /app/docman-gateway.jar ${GATEWAY_PORT:-8080} \
    --spring.profiles.active=distributed \
    --spring.cloud.nacos.discovery.server-addr=${NACOS_HOST:-nacos:8848}

# Start User Service
start_svc "User Service" /app/docman-user.jar ${USER_PORT:-8081} \
    --spring.profiles.active=distributed \
    --spring.cloud.nacos.discovery.server-addr=${NACOS_HOST:-nacos:8848} \
    --spring.datasource.url=jdbc:mysql://${MYSQL_HOST:-mysql}:3306/${MYSQL_DATABASE:-docman_user} \
    --spring.datasource.username=${MYSQL_USER:-root} \
    --spring.datasource.password=${MYSQL_PASSWORD:-root123} \
    --spring.data.redis.host=${REDIS_HOST:-redis} \
    --spring.data.redis.port=${REDIS_PORT:-6379}

# Start Document Service
start_svc "Document Service" /app/docman-document.jar ${DOCUMENT_PORT:-8082} \
    --spring.profiles.active=distributed \
    --spring.cloud.nacos.discovery.server-addr=${NACOS_HOST:-nacos:8848} \
    --spring.datasource.url=jdbc:mysql://${MYSQL_HOST:-mysql}:3306/${MYSQL_DATABASE:-docman_document} \
    --spring.datasource.username=${MYSQL_USER:-root} \
    --spring.datasource.password=${MYSQL_PASSWORD:-root123}

# Start Storage Service
start_svc "Storage Service" /app/docman-storage.jar ${STORAGE_PORT:-8083} \
    --spring.profiles.active=distributed \
    --spring.cloud.nacos.discovery.server-addr=${NACOS_HOST:-nacos:8848} \
    --minio.endpoint=${MINIO_ENDPOINT:-http://minio:9000} \
    --minio.access-key=${MINIO_ACCESS_KEY:-minioadmin} \
    --minio.secret-key=${MINIO_SECRET_KEY:-minioadmin}

# Start Search Service
start_svc "Search Service" /app/docman-search.jar ${SEARCH_PORT:-8084} \
    --spring.profiles.active=distributed \
    --spring.cloud.nacos.discovery.server-addr=${NACOS_HOST:-nacos:8848} \
    --elasticsearch.uris=${ES_HOST:-elasticsearch}:9200

# Start Version Service
start_svc "Version Service" /app/docman-version.jar ${VERSION_PORT:-8085} \
    --spring.profiles.active=distributed \
    --spring.cloud.nacos.discovery.server-addr=${NACOS_HOST:-nacos:8848} \
    --spring.datasource.url=jdbc:mysql://${MYSQL_HOST:-mysql}:3306/${MYSQL_DATABASE:-docman} \
    --spring.datasource.username=${MYSQL_USER:-root} \
    --spring.datasource.password=${MYSQL_PASSWORD:-root123}

# Start Classification Service
start_svc "Classification Service" /app/docman-classification.jar ${CLASSIFICATION_PORT:-8086} \
    --spring.profiles.active=distributed \
    --spring.cloud.nacos.discovery.server-addr=${NACOS_HOST:-nacos:8848} \
    --spring.datasource.url=jdbc:mysql://${MYSQL_HOST:-mysql}:3306/${MYSQL_DATABASE:-docman} \
    --spring.datasource.username=${MYSQL_USER:-root} \
    --spring.datasource.password=${MYSQL_PASSWORD:-root123}

# Start Approval Service
start_svc "Approval Service" /app/docman-approval.jar ${APPROVAL_PORT:-8087} \
    --spring.profiles.active=distributed \
    --spring.cloud.nacos.discovery.server-addr=${NACOS_HOST:-nacos:8848} \
    --spring.datasource.url=jdbc:mysql://${MYSQL_HOST:-mysql}:3306/${MYSQL_DATABASE:-docman} \
    --spring.datasource.username=${MYSQL_USER:-root} \
    --spring.datasource.password=${MYSQL_PASSWORD:-root123}

# Start Subscription Service
start_svc "Subscription Service" /app/docman-subscription.jar ${SUBSCRIPTION_PORT:-8088} \
    --spring.profiles.active=distributed \
    --spring.cloud.nacos.discovery.server-addr=${NACOS_HOST:-nacos:8848} \
    --spring.data.redis.host=${REDIS_HOST:-redis} \
    --spring.data.redis.port=${REDIS_PORT:-6379}

# Start Collaboration Service
start_svc "Collaboration Service" /app/docman-collaboration.jar ${COLLABORATION_PORT:-8089} \
    --spring.profiles.active=distributed \
    --spring.cloud.nacos.discovery.server-addr=${NACOS_HOST:-nacos:8848} \
    --spring.datasource.url=jdbc:mysql://${MYSQL_HOST:-mysql}:3306/${MYSQL_DATABASE:-docman} \
    --spring.datasource.username=${MYSQL_USER:-root} \
    --spring.datasource.password=${MYSQL_PASSWORD:-root123}

# Start CAD Service
start_svc "CAD Service" /app/docman-cad.jar ${CAD_PORT:-8090} \
    --spring.profiles.active=distributed \
    --spring.cloud.nacos.discovery.server-addr=${NACOS_HOST:-nacos:8848} \
    --minio.endpoint=${MINIO_ENDPOINT:-http://minio:9000} \
    --minio.access-key=${MINIO_ACCESS_KEY:-minioadmin} \
    --minio.secret-key=${MINIO_SECRET_KEY:-minioadmin}

# Start System Service
start_svc "System Service" /app/docman-system.jar ${SYSTEM_PORT:-8091} \
    --spring.profiles.active=distributed \
    --spring.cloud.nacos.discovery.server-addr=${NACOS_HOST:-nacos:8848} \
    --spring.datasource.url=jdbc:mysql://${MYSQL_HOST:-mysql}:3306/${MYSQL_DATABASE:-docman} \
    --spring.datasource.username=${MYSQL_USER:-root} \
    --spring.datasource.password=${MYSQL_PASSWORD:-root123}

echo "All services started. Waiting for initialization..."
sleep 10

echo "Running services:"
ps aux | grep java | grep -v grep

# Keep container running
tail -f /dev/null
