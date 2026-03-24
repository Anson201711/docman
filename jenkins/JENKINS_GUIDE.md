# Jenkins 任务创建指南

## 概述

本文档说明如何在 Jenkins (http://192.168.145.252:8089) 上创建 DocMan 项目的部署任务。

## 任务列表

| 任务名称 | 类型 | 描述 |
|---------|------|------|
| docman-middleware | Freestyle | 中间件安装 (MySQL/Redis/ES/MinIO) |
| docman-backend-build | Freestyle | 后端 Maven 构建 + Docker 镜像 |
| docman-backend-deploy | Freestyle | 后端单体部署 |
| docman-frontend-build | Freestyle | 前端 npm 构建 + Docker 镜像 |
| docman-frontend-deploy | Freestyle | 前端部署 |
| docman-full-deploy | Pipeline | 完整一键部署 (推荐) |

## 创建步骤

### 方式一：使用 create-jobs.sh 脚本

```bash
cd /Users/infodba/Documents/01_codes/docman/jenkins
export JENKINS_TOKEN=你的Jenkins API Token
chmod +x create-jobs.sh
./create-jobs.sh
```

### 方式二：手动创建

#### 1. 访问 Jenkins

打开浏览器访问: http://192.168.145.252:8089
使用账号: `infodba` / `infodba.`

#### 2. 创建 Middleware 安装任务

1. 点击 "New Item" (新建任务)
2. 输入任务名称: `docman-middleware`
3. 选择 "Freestyle project"
4. 点击 "OK"

**配置:**
- Description: `DocMan Middleware Installation - MySQL, Redis, Elasticsearch, MinIO`
- Build Triggers: 可选定时任务 `H 2 * * *` (每天凌晨2点)
- Build Steps: Add "Execute shell"

```bash
#!/bin/bash
set -e

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
sleep 30
docker ps | grep docman
```

#### 3. 创建后端构建任务

1. 点击 "New Item"
2. 输入任务名称: `docman-backend-build`
3. 选择 "Freestyle project"
4. 点击 "OK"

**配置:**
- Source Code Management: Git
  - Repository URL: `https://github.com/Anson201711/docman.git`
  - Branch: `*/main`
- Build Triggers: 勾选 "GitHub hook trigger for GITScm polling"
- Build Steps: Add "Execute shell"

```bash
#!/bin/bash
set -e

echo "Building DocMan Backend..."
mvn clean package -Pstandalone -DskipTests -q
docker build -t docman-backend:standalone -f docker/Dockerfile-backend .
docker tag docman-backend:standalone docman-backend:latest
echo "Backend build completed!"
```

#### 4. 创建后端部署任务

1. 点击 "New Item"
2. 输入任务名称: `docman-backend-deploy`
3. 选择 "Freestyle project"
4. 点击 "OK"

**配置:**
- Build Steps: Add "Execute shell"

```bash
#!/bin/bash
set -e

echo "Deploying DocMan Backend..."

# Stop existing
docker stop docman-backend || true
docker rm docman-backend || true

# Run new container
docker run -d \
  --name docman-backend \
  --restart always \
  -p 8080-8091:8080-8091 \
  -e SPRING_PROFILES_ACTIVE=standalone \
  -e MYSQL_HOST=192.168.145.252 \
  -e REDIS_HOST=192.168.145.252 \
  -e MINIO_ENDPOINT=http://192.168.145.252:9000 \
  -e ES_HOST=192.168.145.252 \
  --link docman-mysql \
  --link docman-redis \
  --link docman-elasticsearch \
  --link docman-minio \
  docman-backend:standalone

sleep 20
docker ps | grep docman
```

#### 5. 创建前端构建任务

1. 点击 "New Item"
2. 输入任务名称: `docman-frontend-build`
3. 选择 "Freestyle project"
4. 点击 "OK"

**配置:**
- Source Code Management: Git
  - Repository URL: `https://github.com/Anson201711/docman.git`
  - Branch: `*/main`
- Build Steps: Add "Execute shell"

```bash
#!/bin/bash
set -e

echo "Building DocMan Frontend..."
cd docman-frontend
npm install --legacy-peer-deps
npm run build
cd ..
docker build -t docman-frontend:latest -f docker/Dockerfile-frontend .
echo "Frontend build completed!"
```

#### 6. 创建前端部署任务

1. 点击 "New Item"
2. 输入任务名称: `docman-frontend-deploy`
3. 选择 "Freestyle project"
4. 点击 "OK"

**配置:**
- Build Steps: Add "Execute shell"

```bash
#!/bin/bash
set -e

echo "Deploying DocMan Frontend..."
docker stop docman-frontend || true
docker rm docman-frontend || true

docker run -d \
  --name docman-frontend \
  -p 3000:3000 \
  --restart always \
  -e NEXT_PUBLIC_API_BASE=http://localhost:8080 \
  docman-frontend:latest

docker ps | grep docman
```

#### 7. 创建完整一键部署 Pipeline (推荐)

1. 点击 "New Item"
2. 输入任务名称: `docman-full-deploy`
3. 选择 "Pipeline"
4. 点击 "OK"

**配置:**
- Pipeline: Copy from `Jenkinsfile-full-deploy` content

或者在 Pipeline script 中粘贴以下内容:

```groovy
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Middleware') {
            steps {
                sh '''
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
                    sleep 30
                '''
            }
        }

        stage('Backend Build') {
            steps {
                sh '''
                    mvn clean package -Pstandalone -DskipTests -q
                    docker build -t docman-backend:standalone -f docker/Dockerfile-backend .
                '''
            }
        }

        stage('Backend Deploy') {
            steps {
                sh '''
                    docker stop docman-backend || true
                    docker rm docman-backend || true
                    docker run -d --name docman-backend --restart always -p 8080-8091:8080-8091 -e SPRING_PROFILES_ACTIVE=standalone -e MYSQL_HOST=192.168.145.252 -e REDIS_HOST=192.168.145.252 -e MINIO_ENDPOINT=http://192.168.145.252:9000 -e ES_HOST=192.168.145.252 --link docman-mysql --link docman-redis --link docman-elasticsearch --link docman-minio docman-backend:standalone
                    sleep 20
                '''
            }
        }

        stage('Frontend Build & Deploy') {
            steps {
                sh '''
                    cd docman-frontend && npm install --legacy-peer-deps && npm run build && cd ..
                    docker build -t docman-frontend:latest -f docker/Dockerfile-frontend .
                    docker stop docman-frontend || true
                    docker rm docman-frontend || true
                    docker run -d --name docman-frontend -p 3000:3000 --restart always -e NEXT_PUBLIC_API_BASE=http://localhost:8080 docman-frontend:latest
                '''
            }
        }

        stage('Health Check') {
            steps {
                sh '''
                    curl -sf http://localhost:8080/actuator/health
                    curl -sf http://localhost:3000
                    docker ps | grep docman
                '''
            }
        }
    }
}
```

## 任务执行顺序

推荐使用 `docman-full-deploy` 一键部署，或者按顺序执行:

```
docman-middleware (只需执行一次)
    ↓
docman-backend-build
    ↓
docman-backend-deploy
    ↓
docman-frontend-build
    ↓
docman-frontend-deploy
```

## 部署完成后的服务地址

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:3000 |
| 后端网关 | http://localhost:8080 |
| MySQL | localhost:3306 (root/docman123) |
| Redis | localhost:6379 |
| Elasticsearch | http://localhost:9200 |
| MinIO Console | http://localhost:9001 (minioadmin/minioadmin) |

## 故障排查

### 查看容器日志
```bash
docker logs docman-mysql
docker logs docman-redis
docker logs docman-elasticsearch
docker logs docman-minio
docker logs docman-backend
docker logs docman-frontend
```

### 重启服务
```bash
docker restart docman-mysql
docker restart docman-redis
docker restart docman-elasticsearch
docker restart docman-minio
docker restart docman-backend
docker restart docman-frontend
```

### 清理并重新部署
```bash
docker stop docman-backend docman-frontend || true
docker rm docman-backend docman-frontend || true
docker compose -f docker-compose-middleware.yml down -v
docker system prune -f
```
