#!/bin/bash
# ============================================
# DocMan Jenkins Job Creator
# Creates Jenkins jobs via REST API
# ============================================

set -e

JENKINS_URL="http://192.168.145.252:8089"
JENKINS_USER="infodba"
JENKINS_TOKEN=""  # Should be set from environment or prompt

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() { echo -e "${YELLOW}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Check if curl is available
if ! command -v curl &> /dev/null; then
    log_error "curl is required but not installed"
    exit 1
fi

# Get Jenkinscrumb for CSRF
get_crumb() {
    curl -s -u "${JENKINS_USER}:${JENKINS_TOKEN}" "${JENKINS_URL}/crumbIssuer/api/json" | grep -o '"crumb":"[^"]*"' | cut -d'"' -f4
}

# Check if job exists
job_exists() {
    local job_name="$1"
    curl -s -o /dev/null -w "%{http_code}" -u "${JENKINS_USER}:${JENKINS_TOKEN}" "${JENKINS_URL}/job/${job_name}/config.xml"
}

# Create or update job
create_job() {
    local job_name="$1"
    local job_config="$2"

    log_info "Creating job: ${job_name}"

    if [ "$(job_exists "${job_name}")" = "200" ]; then
        log_info "Job ${job_name} exists, updating..."
        curl -s -u "${JENKINS_USER}:${JENKINS_TOKEN}" \
            -X POST \
            -H "Content-Type: application/xml" \
            --data-binary @"${job_config}" \
            "${JENKINS_URL}/job/${job_name}/config.xml"
    else
        log_info "Creating new job: ${job_name}"
        curl -s -u "${JENKINS_USER}:${JENKINS_TOKEN}" \
            -X POST \
            -H "Content-Type: application/xml" \
            --data-binary @"${job_config}" \
            "${JENKINS_URL}/createItem?name=${job_name}"
    fi

    if [ $? -eq 0 ]; then
        log_success "Job ${job_name} created/updated successfully"
    else
        log_error "Failed to create job ${job_name}"
    fi
}

# Enable/disable job
enable_job() {
    local job_name="$1"
    local enabled="$2"  # true or false

    local action=""
    if [ "$enabled" = "true" ]; then
        action="enable"
    else
        action="disable"
    fi

    curl -s -u "${JENKINS_USER}:${JENKINS_TOKEN}" \
        -X POST \
        "${JENKINS_URL}/job/${job_name}/${action}"
}

log_info "Jenkins Job Creator for DocMan"
log_info "Jenkins URL: ${JENKINS_URL}"
log_info "============================================"

# Check Jenkins connectivity
if ! curl -s -u "${JENKINS_USER}:${JENKINS_TOKEN}" "${JENKINS_URL}/api/json" > /dev/null 2>&1; then
    log_error "Cannot connect to Jenkins at ${JENKINS_URL}"
    log_info "Please ensure Jenkins is running and credentials are correct"
    exit 1
fi

log_success "Connected to Jenkins"

# Create jobs directory
JOBS_DIR="/tmp/docman-jenkins-jobs"
mkdir -p "${JOBS_DIR}"

# ============================================
# Job 1: Middleware Installation
# ============================================
cat > "${JOBS_DIR}/docman-middleware-config.xml" << 'EOF'
<?xml version='1.1' encoding='UTF-8'?>
<project>
    <description>DocMan Middleware Installation - MySQL, Redis, Elasticsearch, MinIO</description>
    <keepDependencies>false</keepDependencies>
    <properties>
        <jenkins.plugins.ownership OwnershipPlugin>
            <owners>
                <ownership userName="infodba" ownership="true"/>
            </owners>
        </jenkins.plugins.ownership OwnershipPlugin>
    </properties>
    <scm class="hudson.scm.NullSCM"/>
    <canRoam>true</canRoam>
    <disabled>false</disabled>
    <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
    <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
    <triggers>
        <hudson.triggers.TimerTrigger>
            <spec>H 2 * * *</spec>
        </hudson.triggers.TimerTrigger>
    </triggers>
    <concurrentBuild>false</concurrentBuild>
    <builders>
        <hudson.tasks.Shell>
            <command>#!/bin/bash
set -e

echo "============================================"
echo "DocMan Middleware Installation"
echo "============================================"

# Docker Compose for Middleware
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
      - minio-data=/data
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

echo "Starting middleware services..."
docker compose -f docker-compose-middleware.yml up -d

echo "Waiting for services to be healthy..."
sleep 30

echo "Checking service status..."
docker compose -f docker-compose-middleware.yml ps

echo "============================================"
echo "Middleware installation completed!"
echo "Services:"
echo "  - MySQL: localhost:3306 (root/docman123)"
echo "  - Redis: localhost:6379"
echo "  - Elasticsearch: localhost:9200"
echo "  - MinIO: localhost:9000 (minioadmin/minioadmin)"
echo "============================================"
</command>
        </hudson.tasks.Shell>
    </builders>
    <publishers>
        <hudson.tasks.Mailer>
            <recipients>infodba@example.com</recipients>
            <dontNotifyEveryUnstableBuild>false</dontNotifyEveryUnstableBuild>
            <sendToIndividuals>false</sendToIndividuals>
        </hudson.tasks.Mailer>
    </publishers>
</project>
EOF

# ============================================
# Job 2: Backend Build
# ============================================
cat > "${JOBS_DIR}/docman-backend-build-config.xml" << 'EOF'
<?xml version='1.1' encoding='UTF-8'?>
<project>
    <description>DocMan Backend Build - Maven Build and Docker Image</description>
    <keepDependencies>false</keepDependencies>
    <properties>
        <jenkins.plugins.ownership OwnershipPlugin>
            <owners>
                <ownership userName="infodba" ownership="true"/>
            </owners>
        </jenkins.plugins.ownership OwnershipPlugin>
    </properties>
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
echo "DocMan Backend Build"
echo "============================================"

# Maven build with standalone profile
mvn clean package -Pstandalone -DskipTests

# Build Docker image
docker build -t docman-backend:standalone -f docker/Dockerfile-backend .

echo "============================================"
echo "Backend build completed!"
echo "Docker image: docman-backend:standalone"
echo "============================================"
</command>
        </hudson.tasks.Shell>
    </builders>
    <publishers>
        <hudson.tasks.Mailer>
            <recipients>infodba@example.com</recipients>
            <dontNotifyEveryUnstableBuild>false</dontNotifyEveryUnstableBuild>
            <sendToIndividuals>false</sendToIndividuals>
        </hudson.tasks.Mailer>
    </publishers>
</project>
EOF

# ============================================
# Job 3: Backend Deploy (Standalone)
# ============================================
cat > "${JOBS_DIR}/docman-backend-deploy-config.xml" << 'EOF'
<?xml version='1.1' encoding='UTF-8'?>
<project>
    <description>DocMan Backend Deploy - Standalone Mode</description>
    <keepDependencies>false</keepDependencies>
    <properties>
        <jenkins.plugins.ownership OwnershipPlugin>
            <owners>
                <ownership userName="infodba" ownership="true"/>
            </owners>
        </jenkins.plugins.ownership OwnershipPlugin>
    </properties>
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

echo "============================================"
echo "DocMan Backend Deploy (Standalone)"
echo "============================================"

# Stop existing container
docker stop docman-backend || true
docker rm docman-backend || true

# Run backend with docker-compose
cat > docker-compose-backend.yml << 'COMPOSE'
version: '3.8'
services:
  backend:
    image: docman-backend:standalone
    container_name: docman-backend
    restart: always
    ports:
      - "8080:8080"
      - "8081:8081"
      - "8082:8082"
      - "8083:8083"
      - "8084:8084"
      - "8085:8085"
      - "8086:8086"
      - "8087:8087"
      - "8088:8088"
      - "8089:8089"
      - "8090:8090"
      - "8091:8091"
    environment:
      - SPRING_PROFILES_ACTIVE=standalone
      - MYSQL_HOST=192.168.145.252
      - REDIS_HOST=192.168.145.252
      - MINIO_ENDPOINT=192.168.145.252:9000
      - ES_HOST=192.168.145.252
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_started
      elasticsearch:
        condition: service_healthy
      minio:
        condition: service_started
COMPOSE

echo "Starting backend services..."
docker compose -f docker-compose-backend.yml up -d

echo "Waiting for services to start..."
sleep 20

echo "============================================"
echo "Backend deployment completed!"
echo "Gateway: http://localhost:8080"
echo "API Docs: http://localhost:8080/swagger-ui.html"
echo "============================================"

# Show running containers
docker ps | grep docman
</command>
        </hudson.tasks.Shell>
    </builders>
    <publishers>
        <hudson.tasks.Mailer>
            <recipients>infodba@example.com</recipients>
            <dontNotifyEveryUnstableBuild>false</dontNotifyEveryUnstableBuild>
            <sendToIndividuals>false</sendToIndividuals>
        </hudson.tasks.Mailer>
    </publishers>
</project>
EOF

# ============================================
# Job 4: Frontend Build
# ============================================
cat > "${JOBS_DIR}/docman-frontend-build-config.xml" << 'EOF'
<?xml version='1.1' encoding='UTF-8'?>
<project>
    <description>DocMan Frontend Build - Next.js Build and Docker Image</description>
    <keepDependencies>false</keepDependencies>
    <properties>
        <jenkins.plugins.ownership OwnershipPlugin>
            <owners>
                <ownership userName="infodba" ownership="true"/>
            </owners>
        </jenkins.plugins.ownership OwnershipPlugin>
    </properties>
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
echo "DocMan Frontend Build"
echo "============================================"

cd docman-frontend

# Install dependencies
npm install

# Build
npm run build

# Go back to root
cd ..

# Build Docker image
docker build -t docman-frontend:latest -f docker/Dockerfile-frontend .

echo "============================================"
echo "Frontend build completed!"
echo "Docker image: docman-frontend:latest"
echo "============================================"
</command>
        </hudson.tasks.Shell>
    </builders>
    <publishers>
        <hudson.tasks.Mailer>
            <recipients>infodba@example.com</recipients>
            <dontNotifyEveryUnstableBuild>false</dontNotifyEveryUnstableBuild>
            <sendToIndividuals>false</sendToIndividuals>
        </hudson.tasks.Mailer>
    </publishers>
</project>
EOF

# ============================================
# Job 5: Frontend Deploy (Standalone)
# ============================================
cat > "${JOBS_DIR}/docman-frontend-deploy-config.xml" << 'EOF'
<?xml version='1.1' encoding='UTF-8'?>
<project>
    <description>DocMan Frontend Deploy - Standalone Mode</description>
    <keepDependencies>false</keepDependencies>
    <properties>
        <jenkins.plugins.ownership OwnershipPlugin>
            <owners>
                <ownership userName="infodba" ownership="true"/>
            </owners>
        </jenkins.plugins.ownership OwnershipPlugin>
    </properties>
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

echo "============================================"
echo "DocMan Frontend Deploy (Standalone)"
echo "============================================"

# Stop existing container
docker stop docman-frontend || true
docker rm docman-frontend || true

# Run frontend
docker run -d \
  --name docman-frontend \
  -p 3000:3000 \
  --restart always \
  -e NEXT_PUBLIC_API_BASE=http://localhost:8080 \
  docman-frontend:latest

echo "============================================"
echo "Frontend deployment completed!"
echo "Application: http://localhost:3000"
echo "============================================"

docker ps | grep docman
</command>
        </hudson.tasks.Shell>
    </builders>
    <publishers>
        <hudson.tasks.Mailer>
            <recipients>infodba@example.com</recipients>
            <dontNotifyEveryUnstableBuild>false</dontNotifyEveryUnstableBuild>
            <sendToIndividuals>false</sendToIndividuals>
        </hudson.tasks.Mailer>
    </publishers>
</project>
EOF

# ============================================
# Job 6: Full Deployment (All-in-One)
# ============================================
cat > "${JOBS_DIR}/docman-full-deploy-config.xml" << 'EOF'
<?xml version='1.1' encoding='UTF-8'?>
<project>
    <description>DocMan Full Deployment - Middleware + Backend + Frontend</description>
    <keepDependencies>false</keepDependencies>
    <properties>
        <jenkins.plugins.ownership OwnershipPlugin>
            <owners>
                <ownership userName="infodba" ownership="true"/>
            </owners>
        </jenkins.plugins.ownership OwnershipPlugin>
    </properties>
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
echo "DocMan Full Deployment"
echo "============================================"

# Step 1: Middleware
echo "Step 1/5: Installing middleware..."

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

volumes:
  mysql-data:
  redis-data:
  es-data:
  minio-data:
COMPOSE

docker compose -f docker-compose-middleware.yml up -d
echo "Middleware started, waiting 30s for initialization..."
sleep 30

# Step 2: Backend Build
echo "Step 2/5: Building backend..."
mvn clean package -Pstandalone -DskipTests -q
docker build -t docman-backend:standalone -f docker/Dockerfile-backend . -q

# Step 3: Backend Deploy
echo "Step 3/5: Deploying backend..."
docker stop docman-backend 2>/dev/null || true
docker rm docman-backend 2>/dev/null || true

docker run -d \
  --name docman-backend \
  --restart always \
  -p 8080-8091:8080-8091 \
  -e SPRING_PROFILES_ACTIVE=standalone \
  -e MYSQL_HOST=192.168.145.252 \
  -e REDIS_HOST=192.168.145.252 \
  -e MINIO_ENDPOINT=192.168.145.252:9000 \
  -e ES_HOST=192.168.145.252 \
  --link docman-mysql \
  --link docman-redis \
  --link docman-elasticsearch \
  --link docman-minio \
  docman-backend:standalone

# Step 4: Frontend Build
echo "Step 4/5: Building frontend..."
cd docman-frontend
npm install --legacy-peer-deps
npm run build
cd ..
docker build -t docman-frontend:latest -f docker/Dockerfile-frontend . -q

# Step 5: Frontend Deploy
echo "Step 5/5: Deploying frontend..."
docker stop docman-frontend 2>/dev/null || true
docker rm docman-frontend 2>/dev/null || true

docker run -d \
  --name docman-frontend \
  -p 3000:3000 \
  --restart always \
  -e NEXT_PUBLIC_API_BASE=http://localhost:8080 \
  docman-frontend:latest

echo "============================================"
echo "Deployment completed!"
echo "============================================"
echo "Services:"
echo "  - Frontend: http://localhost:3000"
echo "  - Gateway: http://localhost:8080"
echo "  - MySQL: localhost:3306"
echo "  - Redis: localhost:6379"
echo "  - Elasticsearch: localhost:9200"
echo "  - MinIO: localhost:9000"
echo "============================================"

docker ps | grep docman
</command>
        </hudson.tasks.Shell>
    </builders>
    <publishers>
        <hudson.tasks.Mailer>
            <recipients>infodba@example.com</recipients>
            <dontNotifyEveryUnstableBuild>false</dontNotifyEveryUnstableBuild>
            <sendToIndividuals>false</sendToIndividuals>
        </hudson.tasks.Mailer>
    </publishers>
</project>
EOF

# ============================================
# Create jobs in Jenkins
# ============================================
log_info "Jenkins job configuration files created in: ${JOBS_DIR}"
log_info ""
log_info "To create jobs in Jenkins, you have two options:"
log_info ""
log_info "Option 1: Manual import via Jenkins UI"
log_info "  1. Go to Jenkins: ${JENKINS_URL}"
log_info "  2. Click 'New Item'"
log_info "  3. Enter job name and select 'Copy from existing item'"
log_info "  4. Copy config from files in ${JOBS_DIR}"
log_info ""
log_info "Option 2: Use Jenkins CLI"
log_info "  java -jar jenkins-cli.jar -s ${JENKINS_URL} -auth infodba:TOKEN create-job JOB_NAME < config.xml"
log_info ""
log_info "Job configuration files:"
echo "  - ${JOBS_DIR}/docman-middleware-config.xml"
echo "  - ${JOBS_DIR}/docman-backend-build-config.xml"
echo "  - ${JOBS_DIR}/docman-backend-deploy-config.xml"
echo "  - ${JOBS_DIR}/docman-frontend-build-config.xml"
echo "  - ${JOBS_DIR}/docman-frontend-deploy-config.xml"
echo "  - ${JOBS_DIR}/docman-full-deploy-config.xml"

# Try to create jobs automatically
if [ -n "${JENKINS_TOKEN}" ]; then
    log_info ""
    log_info "Attempting to create jobs automatically..."

    for config in "${JOBS_DIR}"/*-config.xml; do
        job_name=$(basename "$config" | sed 's/-config.xml//')
        create_job "$job_name" "$config"
    done
else
    log_warn ""
    log_warn "JENKINS_TOKEN not set. Set it to enable automatic job creation:"
    log_warn "  export JENKINS_TOKEN=your_token_here"
fi

log_success ""
log_success "Done! Job configurations are ready."
