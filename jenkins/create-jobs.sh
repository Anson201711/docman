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

# ============================================
# Job 3: Smoke Test with HTML Report
# ============================================
cat > "${JOBS_DIR}/docman-smoke-test-config.xml" << 'EOF'
<?xml version='1.1' encoding='UTF-8'?>
<project>
    <description>DocMan Smoke Test - Backend + Frontend Health Checks with HTML Report</description>
    <keepDependencies>false</keepDependencies>
    <properties>
        <jenkins.model.BuildDiscarderProperty>
            <strategy class="hudson.tasks.LogRotator">
                <daysToKeep>30</daysToKeep>
                <numToKeep>50</numToKeep>
            </strategy>
        </jenkins.model.BuildDiscarderProperty>
    </properties>
    <scm class="hudson.scm.NullSCM"/>
    <canRoam>true</canRoam>
    <disabled>false</disabled>
    <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
    <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
    <triggers/>
    <concurrentBuild>false</concurrentBuild>
    <builders>
        <hudson.tasks.Shell">
            <command>#!/bin/bash
set -e

GATEWAY_URL="${GATEWAY_URL:-http://192.168.145.252:9080}"
FRONTEND_URL="${FRONTEND_URL:-http://192.168.145.252:3000}"
ES_HOST="${ES_HOST:-192.168.145.252}"
MINIO_HOST="${MINIO_HOST:-192.168.145.252}"
REPORT_DIR="${WORKSPACE}/test-reports"

echo "============================================"
echo "DocMan 冒烟测试"
echo "============================================"
echo "Gateway: $GATEWAY_URL"
echo "Frontend: $FRONTEND_URL"
echo ""

# 初始化
rm -rf "${REPORT_DIR}"
mkdir -p "${REPORT_DIR}"
rm -f "${WORKSPACE}/test-results.txt"

# 测试函数
test_endpoint() {
    local name="$1"
    local url="$2"
    local expected_code="$3"

    local actual_code=$(curl -sf -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000")
    local result="FAIL"
    local detail=""

    if [ "$actual_code" = "$expected_code" ]; then
        result="PASS"
        detail="HTTP $actual_code"
    else
        detail="期望: $expected_code, 实际: $actual_code"
    fi

    echo "$name|$result|$detail" >> "${WORKSPACE}/test-results.txt"
    [ "$result" = "PASS" ] && echo "✅ $name: $result" || echo "❌ $name: $result ($detail)"
}

# 中间件测试
echo "--- 中间件服务 ---"

# MySQL
echo -n "MySQL: "
if docker exec docman-mysql mysqladmin ping -h localhost -u root -proot123 &>/dev/null; then
    echo "MYSQL|PASS|MySQL 连接成功" >> "${WORKSPACE}/test-results.txt"
    echo "✅ PASS"
else
    echo "MYSQL|FAIL|MySQL 连接失败" >> "${WORKSPACE}/test-results.txt"
    echo "❌ FAIL"
fi

# Redis
echo -n "Redis: "
if docker exec docman-redis redis-cli ping 2>/dev/null | grep -q PONG; then
    echo "REDIS|PASS|Redis 连接成功" >> "${WORKSPACE}/test-results.txt"
    echo "✅ PASS"
else
    echo "REDIS|FAIL|Redis 连接失败" >> "${WORKSPACE}/test-results.txt"
    echo "❌ FAIL"
fi

# Elasticsearch
echo -n "Elasticsearch: "
if curl -sf "http://${ES_HOST}:9200/_cluster/health" 2>/dev/null | grep -q "status"; then
    echo "ELASTICSEARCH|PASS|Elasticsearch 集群健康" >> "${WORKSPACE}/test-results.txt"
    echo "✅ PASS"
else
    echo "ELASTICSEARCH|FAIL|Elasticsearch 无响应" >> "${WORKSPACE}/test-results.txt"
    echo "❌ FAIL"
fi

# MinIO
echo -n "MinIO: "
if curl -sf "http://${MINIO_HOST}:9000/minio/health/live" 2>/dev/null | grep -q "true"; then
    echo "MINIO|PASS|MinIO 服务正常" >> "${WORKSPACE}/test-results.txt"
    echo "✅ PASS"
else
    echo "MINIO|FAIL|MinIO 无响应" >> "${WORKSPACE}/test-results.txt"
    echo "❌ FAIL"
fi

# 后端服务测试
echo ""
echo "--- 后端服务 ---"
test_endpoint "Gateway" "$GATEWAY_URL/actuator/health" "200"
test_endpoint "User Service" "$GATEWAY_URL/api/auth/health" "200"
test_endpoint "Document Service" "$GATEWAY_URL/api/documents/health" "200"
test_endpoint "Storage Service" "$GATEWAY_URL/api/storage/health" "200"
test_endpoint "Search Service" "$GATEWAY_URL/api/search/health" "200"

# 前端测试
echo ""
echo "--- 前端服务 ---"
test_endpoint "Frontend" "$FRONTEND_URL" "200"

# 生成 HTML 报告
echo ""
echo "--- 生成测试报告 ---"

TOTAL=$(wc -l < "${WORKSPACE}/test-results.txt")
PASSED=$(grep -c "|PASS|" "${WORKSPACE}/test-results.txt" || echo "0")
FAILED=$(grep -c "|FAIL|" "${WORKSPACE}/test-results.txt" || echo "0")
TIMESTAMP=$(date "+%Y-%m-%d %H:%M:%S")

# 生成中间件行
MIDDLEWARE_ROWS=""
for entry in "MYSQL|MySQL" "REDIS|Redis" "ELASTICSEARCH|Elasticsearch" "MINIO|MinIO"; do
    name="${entry%%|*}"
    display="${entry##|*}"
    result=$(grep "^$name|" "${WORKSPACE}/test-results.txt" | cut -d'|' -f2)
    detail=$(grep "^$name|" "${WORKSPACE}/test-results.txt" | cut -d'|' -f3)
    [ "$result" = "FAIL" ] && status_class="fail" || status_class="pass"
    MIDDLEWARE_ROWS="${MIDDLEWARE_ROWS}
                        <tr><td>${display}</td><td><span class='status ${status_class}'>${result}</span></td><td>${detail}</td></tr>"
done

# 生成后端服务行
BACKEND_ROWS=""
for entry in "Gateway|Gateway" "User Service|User Service" "Document Service|Document Service" "Storage Service|Storage Service" "Search Service|Search Service" "Frontend|Frontend"; do
    name="${entry%%|*}"
    display="${entry##|*}"
    result=$(grep "^$name|" "${WORKSPACE}/test-results.txt" | cut -d'|' -f2)
    detail=$(grep "^$name|" "${WORKSPACE}/test-results.txt" | cut -d'|' -f3)
    [ "$result" = "FAIL" ] && status_class="fail" || status_class="pass"
    BACKEND_ROWS="${BACKEND_ROWS}
                        <tr><td>${display}</td><td><span class='status ${status_class}'>${result}</span></td><td>${detail}</td></tr>"
done

cat > "${REPORT_DIR}/smoke-test-report.html" << HTML
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>DocMan 冒烟测试报告</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; padding: 20px; }
        .container { max-width: 1200px; margin: 0 auto; background: white; border-radius: 16px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); overflow: hidden; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px; text-align: center; }
        .header h1 { font-size: 2.5em; margin-bottom: 10px; }
        .summary { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; padding: 30px; background: #f8f9fa; }
        .card { background: white; padding: 20px; border-radius: 12px; text-align: center; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
        .card.total { border-left: 4px solid #667eea; }
        .card.passed { border-left: 4px solid #28a745; }
        .card.failed { border-left: 4px solid #dc3545; }
        .card .num { font-size: 3em; font-weight: bold; color: #333; }
        .card.passed .num { color: #28a745; }
        .card.failed .num { color: #dc3545; }
        .card .label { color: #666; margin-top: 5px; }
        .content { padding: 30px; }
        .section-title { font-size: 1.5em; color: #333; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid #667eea; }
        table { width: 100%; border-collapse: collapse; margin-bottom: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); border-radius: 8px; overflow: hidden; }
        th { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px; text-align: left; }
        td { padding: 12px 15px; border-bottom: 1px solid #eee; }
        tr:hover { background: #f8f9fa; }
        .status { display: inline-block; padding: 5px 15px; border-radius: 20px; font-weight: bold; font-size: 0.9em; }
        .status.pass { background: #d4edda; color: #155724; }
        .status.fail { background: #f8d7da; color: #721c24; }
        .footer { background: #f8f9fa; padding: 20px; text-align: center; color: #666; border-top: 1px solid #eee; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🏠 DocMan 冒烟测试报告</h1>
            <p>测试时间: ${TIMESTAMP}</p>
        </div>
        <div class="summary">
            <div class="card total"><div class="num">${TOTAL}</div><div class="label">总测试数</div></div>
            <div class="card passed"><div class="num">${PASSED}</div><div class="label">通过</div></div>
            <div class="card failed"><div class="num">${FAILED}</div><div class="label">失败</div></div>
        </div>
        <div class="content">
            <div class="section-title">🔧 服务健康检查</div>
            <table>
                <thead><tr><th>服务</th><th>状态</th><th>详情</th></tr></thead>
                <tbody>
${MIDDLEWARE_ROWS}
${BACKEND_ROWS}
                </tbody>
            </table>
        </div>
        <div class="footer">
            <p>Generated by DocMan Jenkins Pipeline</p>
            <p>环境: ${GATEWAY_URL}</p>
        </div>
    </div>
</body>
</html>
HTML

echo "============================================"
echo "测试完成!"
echo "总计: $TOTAL | 通过: $PASSED | 失败: $FAILED"
echo "报告: ${REPORT_DIR}/smoke-test-report.html"
echo "============================================"

# 如果有失败，退出码为1
[ "$FAILED" -gt "0" ] && exit 1 || exit 0</command>
        </hudson.tasks.Shell>
    </builders>
    <publishers>
        <htmlpublisher.HtmlPublisher>
            <reportTargets>
                <htmlpublisher.HtmlPublisherTarget>
                    <reportName>测试报告</reportName>
                    <htmlDir>${WORKSPACE}/test-reports</htmlDir>
                    <keepAll>true</keepAll>
                    <wrapperName>htmlwrapper</wrapperName>
                </htmlpublisher.HtmlPublisherTarget>
            </reportTargets>
        </htmlpublisher.HtmlPublisher>
        <hudson.tasks.Mailer>
            <recipients>admin@example.com</recipients>
            <dontNotifyEveryUnstableBuild>false</dontNotifyEveryUnstableBuild>
            <sendToIndividuals>false</sendToIndividuals>
        </hudson.tasks.Mailer>
    </publishers>
</project>
EOF
echo -e "  ${GREEN}✓${NC} Created docman-smoke-test-config.xml (with HTML report)"

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
echo -e "${YELLOW}Step 3:${NC} Create 'docman-smoke-test' job (RECOMMENDED)"
echo "       1. Click 'New Item'"
echo "       2. Name: ${CYAN}docman-smoke-test${NC}"
echo "       3. Type: Freestyle project"
echo "       4. Click 'OK'"
echo "       5. Copy content from: ${CYAN}${JOBS_DIR}/docman-smoke-test-config.xml${NC}"
echo "       6. Paste into 'Raw Build Configuration' section"
echo "       7. Click 'Save'"
echo ""
echo -e "${YELLOW}Step 4:${NC} After running build, view HTML report:"
echo "       - Click on build number"
echo "       - Click '测试报告' in left menu"
echo "       - Or access: ${CYAN}${JENKINS_URL}/job/docman-smoke-test/lastBuild/test-reports/${NC}"
echo ""
echo -e "${GREEN}Config files are ready at: ${JOBS_DIR}${NC}"
echo ""
echo -e "${BLUE}============================================${NC}"
echo -e "${GREEN}  Setup Complete!${NC}"
echo -e "${BLUE}============================================${NC}"
