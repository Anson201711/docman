#!/bin/bash
#
# DocMan Standalone Start Script
# Single JAR deployment
#

set -e

echo "============================================"
echo "  DocMan Standalone - Starting"
echo "============================================"

# Configuration with defaults
MYSQL_HOST=${MYSQL_HOST:-mysql}
MYSQL_PORT=${MYSQL_PORT:-3306}
MYSQL_DATABASE=${MYSQL_DATABASE:-docman}
MYSQL_USER=${MYSQL_USER:-root}
MYSQL_PASSWORD=${MYSQL_PASSWORD:-root123}

REDIS_HOST=${REDIS_HOST:-redis}
REDIS_PORT=${REDIS_PORT:-6379}
REDIS_PASSWORD=${REDIS_PASSWORD:-}

GATEWAY_PORT=${GATEWAY_PORT:-8080}

# Find the JAR file
JAR_FILE=$(ls /app/*.jar 2>/dev/null | head -1)

if [ -z "$JAR_FILE" ]; then
    echo "ERROR: No JAR file found in /app/"
    exit 1
fi

echo "Using JAR: $JAR_FILE"
echo "Gateway Port: $GATEWAY_PORT"
echo "MySQL: $MYSQL_HOST:$MYSQL_PORT/$MYSQL_DATABASE"
echo "Redis: $REDIS_HOST:$REDIS_PORT"

# Start the application
exec java -jar "$JAR_FILE" \
    --server.port=$GATEWAY_PORT \
    --spring.profiles.active=standalone \
    --spring.datasource.url="jdbc:mysql://$MYSQL_HOST:$MYSQL_PORT/$MYSQL_DATABASE?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true" \
    --spring.datasource.username=$MYSQL_USER \
    --spring.datasource.password=$MYSQL_PASSWORD \
    --spring.data.redis.host=$REDIS_HOST \
    --spring.data.redis.port=$REDIS_PORT \
    --spring.data.redis.password=$REDIS_PASSWORD
