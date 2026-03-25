#!/bin/bash
# ============================================
# DocMan Jenkins Job Creator
# Generates job configs and provides import instructions
# ============================================

JENKINS_URL="${JENKINS_URL:-http://192.168.145.252:9089}"
JENKINS_USER="${JENKINS_USER:-infodba}"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  DocMan Jenkins Job Creator${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""
echo -e "${YELLOW}Jenkins URL:${NC} ${JENKINS_URL}"
echo -e "${YELLOW}User:${NC} ${JENKINS_USER}"
echo ""

# Create jobs directory
JOBS_DIR="/tmp/docman-jenkins-jobs"
mkdir -p "${JOBS_DIR}"

echo -e "${YELLOW}[INFO]${NC} Generating job configuration files..."

# ============================================
# Job 1: Middleware Installation
# ============================================
cat > "${JOBS_DIR}/docman-middleware-config.xml" << 'EOF'
<?xml version='1.1' encoding='UTF-8'?>
<project>
    <description>DocMan Middleware Installation - MySQL, Redis, Elasticsearch, MinIO</description>
    <keepDependencies>false</keepDependencies>
    <scm class="hudson.scm.NullSCM"/>
    <canRoam>true</canRoam>
    <disabled>false</disabled>
    <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
    <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
    <triggers/>
    <concurrentBuild>false</concurrentBuild>
    <builders>
        <hudson.tasks.Shell>
            <command>#!/bin/bash
set -e
cat > docker-compose-middleware.yml << 'COMPOSE'
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: docman-mysql
    restart: always
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: docman123
      MYSQL_DATABASE: docman
      MYSQL_USER: docman
      MYSQL_PASSWORD: docman123
    volumes:
      - mysql-data:/var/lib/mysql
      - ./docker/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
  redis:
    image: redis:7-alpine
    container_name: docman-redis
    restart: always
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
  elasticsearch:
    image: elasticsearch:8.12.0
    container_name: docman-elasticsearch
    restart: always
    ports:
      - "9200:9200"
      - "9300:9300"
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms2g -Xmx2g"
    volumes:
      - es-data:/usr/share/elasticsearch/data
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:9200/_cluster/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5
  minio:
    image: minio/minio:latest
    container_name: docman-minio
    restart: always
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    volumes:
      - minio-data:/data
    command: server /data --console-address ":9001"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 20s
      retries: 3
volumes:
  mysql-data:
  redis-data:
  es-data:
  minio-data:
COMPOSE
docker compose -f docker-compose-middleware.yml up -d
echo "Waiting for middleware initialization..."
sleep 30
docker ps | grep docman
echo "Middleware installation completed!"</command>
        </hudson.tasks.Shell>
    </builders>
</project>
EOF
echo -e "  ${GREEN}✓${NC} Created docman-middleware-config.xml"

# ============================================
# Job 2: Full Deployment Pipeline
# ============================================
cat > "${JOBS_DIR}/docman-full-deploy-config.xml" << 'EOF'
<?xml version='1.1' encoding='UTF-8'?>
<project>
    <description>DocMan Full Deployment Pipeline - Middleware + Backend + Frontend + Smoke Tests</description>
    <keepDependencies>false</keepDependencies>
    <scm class="hudson.plugins.git.GitSCM">
        <configVersion>2</configVersion>
        <userRemoteConfigs>
            <hudson.plugins.git.UserRemoteConfig>
                <url>https://github.com/Anson201711/docman.git</url>
            </hudson.plugins.git.UserRemoteConfig>
        </userRemoteConfigs>
        <branches>
            <hudson.plugins.git.BranchSpec>
                <name>*/main</name>
            </hudson.plugins.git.BranchSpec>
        </branches>
    </scm>
    <canRoam>true</canRoam>
    <disabled>false</disabled>
    <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
    <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
    <triggers>
        <hudson.triggers.GitHubTRigger>
            <spec>*/main</spec>
        </hudson.triggers.GitHubTRigger>
    </triggers>
    <concurrentBuild>false</concurrentBuild>
    <builders>
        <hudson.tasks.Shell>
            <command>#!/bin/bash
set -e

echo "============================================"
echo "DocMan Full Deployment Pipeline"
echo "============================================"

# Step 1: Middleware
echo "Step 1/6: Installing middleware..."
cat > docker-compose-middleware.yml << 'COMPOSE'
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: docman-mysql
    restart: always
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: docman123
      MYSQL_DATABASE: docman
      MYSQL_USER: docman
      MYSQL_PASSWORD: docman123
    volumes:
      - mysql-data:/var/lib/mysql
      - ./docker/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
  redis:
    image: redis:7-alpine
    container_name: docman-redis
    restart: always
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
  elasticsearch:
    image: elasticsearch:8.12.0
    container_name: docman-elasticsearch
    restart: always
    ports:
      - "9200:9200"
      - "9300:9300"
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms2g -Xmx2g"
    volumes:
      - es-data:/usr/share/elasticsearch/data
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:9200/_cluster/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5
  minio:
    image: minio/minio:latest
    container_name: docman-minio
    restart: always
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    volumes:
      - minio-data:/data
    command: server /data --console-address ":9001"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 20s
      retries: 3
volumes:
  mysql-data:
  redis-data:
  es-data:
  minio-data:
COMPOSE
docker compose -f docker-compose-middleware.yml up -d
sleep 30

# Step 2: Backend Build
echo "Step 2/6: Building backend..."
mvn clean package -Pstandalone -DskipTests -q
docker build -t docman-backend:standalone -f docker/Dockerfile-backend . -q

# Step 3: Backend Deploy
echo "Step 3/6: Deploying backend..."
docker stop docman-backend 2>/dev/null || true
docker rm docman-backend 2>/dev/null || true
docker run -d \
  --name docman-backend \
  --restart always \
  -p 8080-8091:8080-8091 \
  -e SPRING_PROFILES_ACTIVE=standalone \
  -e MYSQL_HOST=192.168.145.252 \
  -e REDIS_HOST=192.168.145.252 \
  -e MINIO_ENDPOINT=http://192.168.145.252:9000 \
  -e ES_HOST=192.168.145.252 \
  --link docman-mysql --link docman-redis \
  --link docman-elasticsearch --link docman-minio \
  docman-backend:standalone
sleep 20

# Step 4: Frontend Build
echo "Step 4/6: Building frontend..."
cd docman-frontend
npm install --legacy-peer-deps
npm run build
cd ..
docker build -t docman-frontend:latest -f docker/Dockerfile-frontend . -q

# Step 5: Frontend Deploy
echo "Step 5/6: Deploying frontend..."
docker stop docman-frontend 2>/dev/null || true
docker rm docman-frontend 2>/dev/null || true
docker run -d \
  --name docman-frontend \
  -p 3000:3000 \
  --restart always \
  -e NEXT_PUBLIC_API_BASE=http://localhost:8080 \
  docman-frontend:latest

# Step 6: Smoke Tests
echo "Step 6/6: Running smoke tests..."

# Test MySQL
echo -n "MySQL: "
docker exec docman-mysql mysqladmin ping -h localhost -u root -pdocman123 2>/dev/null | grep -q "mysqld is alive" && echo "OK" || echo "FAIL"

# Test Redis
echo -n "Redis: "
docker exec docman-redis redis-cli ping 2>/dev/null | grep -q PONG && echo "OK" || echo "FAIL"

# Test Elasticsearch
echo -n "Elasticsearch: "
curl -sf http://localhost:9200/_cluster/health 2>/dev/null | grep -q '"status"' && echo "OK" || echo "FAIL"

# Test MinIO
echo -n "MinIO: "
curl -sf http://localhost:9000/minio/health/live 2>/dev/null && echo "OK" || echo "FAIL"

# Test Backend
echo -n "Backend: "
curl -sf http://localhost:8080/actuator/health 2>/dev/null && echo "OK" || echo "FAIL"

# Test Frontend
echo -n "Frontend: "
curl -sf http://localhost:3000 2>/dev/null | head -c 100 | grep -qi "html" && echo "OK" || echo "FAIL"

echo "============================================"
echo "Deployment completed!"
echo "Services:"
echo "  - Frontend: http://localhost:3000"
echo "  - Gateway: http://localhost:8080"
echo "  - MySQL: localhost:3306"
echo "  - Redis: localhost:6379"
echo "  - Elasticsearch: localhost:9200"
echo "  - MinIO: localhost:9000"
echo "============================================"
docker ps | grep docman</command>
        </hudson.tasks.Shell>
    </builders>
</project>
EOF
echo -e "  ${GREEN}✓${NC} Created docman-full-deploy-config.xml"

echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}  MANUAL JOB CREATION INSTRUCTIONS${NC}"
echo -e "${BLUE}============================================${NC}"
echo ""
echo -e "${YELLOW}Step 1:${NC} Open Jenkins Web UI"
echo -e "       URL: ${CYAN}${JENKINS_URL}${NC}"
echo -e "       User: ${JENKINS_USER}"
echo ""
echo -e "${YELLOW}Step 2:${NC} Create 'docman-middleware' job"
echo "       1. Click 'New Item'"
echo "       2. Name: ${CYAN}docman-middleware${NC}"
echo "       3. Type: Freestyle project"
echo "       4. Click 'OK'"
echo "       5. Copy content from: ${CYAN}${JOBS_DIR}/docman-middleware-config.xml${NC}"
echo "       6. Paste into 'Raw Build Configuration' section"
echo "       7. Click 'Save'"
echo ""
echo -e "${YELLOW}Step 3:${NC} Create 'docman-full-deploy' job (RECOMMENDED)"
echo "       1. Click 'New Item'"
echo "       2. Name: ${CYAN}docman-full-deploy${NC}"
echo "       3. Type: Freestyle project (NOT Pipeline)"
echo "       4. Click 'OK'"
echo "       5. Under 'Source Code Management', select Git"
echo "          Repository URL: ${CYAN}https://github.com/Anson201711/docman.git${NC}"
echo "          Branch: ${CYAN}*/main${NC}"
echo "       6. Under 'Build Triggers', check 'GitHub hook trigger'"
echo "       7. Copy content from: ${CYAN}${JOBS_DIR}/docman-full-deploy-config.xml${NC}"
echo "       8. Paste into 'Raw Build Configuration' section"
echo "       9. Click 'Save'"
echo ""
echo -e "${GREEN}Config files are ready at: ${JOBS_DIR}${NC}"
echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${GREEN}  Setup Complete!${NC}"
echo -e "${BLUE}============================================${NC}"
