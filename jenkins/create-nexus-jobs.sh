#!/bin/bash
# ============================================
# DocMan Jenkins Job 创建脚本
# 创建完整的 DevOps 工作流 Jobs
# ============================================

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Jenkins 配置
JENKINS_URL="${JENKINS_URL:-http://localhost:8080}"
JENKINS_USER="${JENKINS_USER:-admin}"
JENKINS_TOKEN="${JENKINS_TOKEN:-}"
JOBS_DIR="/var/jenkins_jobs"

# 创建目录
mkdir -p "${JOBS_DIR}"

echo -e "${BLUE}=== DocMan Jenkins Jobs 创建脚本 ===${NC}"
echo "Jenkins URL: ${JENKINS_URL}"
echo ""

# Job 1: Nexus 部署
create_nexus_job() {
    echo -e "${GREEN}创建 Job: docman-nexus-deploy${NC}"

    cat > "${JOBS_DIR}/docman-nexus-deploy.xml" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <description>部署 Nexus 私有仓库</description>
    <keepDependencies>false</keepDependencies>
    <properties>
        <jenkins.model.BuildDiscarderProperty>
            <strategy class="hudson.tasks.LogRotator">
                <daysToKeep>7</daysToKeep>
                <numToKeep>10</numToKeep>
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
        <inheritedWSOnMasterBuildWrapper/>
    </builders>
    <publishers/>
    <buildWrappers/>
</project>
EOF

    # 使用 Jenkins CLI 或 API 创建 job
    echo "Nexus 部署 Job XML 已生成: ${JOBS_DIR}/docman-nexus-deploy.xml"
}

# Job 2: 后端构建
create_backend_build_job() {
    echo -e "${GREEN}创建 Job: docman-backend-build${NC}"

    cat > "${JOBS_DIR}/docman-backend-build.xml" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <description>后端 Maven 构建 + Docker 镜像</description>
    <keepDependencies>false</keepDependencies>
    <properties>
        <jenkins.model.BuildDiscarderProperty>
            <strategy class="hudson.tasks.LogRotator">
                <daysToKeep>30</daysToKeep>
                <numToKeep>50</numToKeep>
            </strategy>
        </jenkins.model.BuildDiscarderProperty>
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
    <blockBuildWhenUpstreamBuilding>true</blockBuildWhenUpstreamBuilding>
    <triggers>
        <hudson.triggers.SCMTrigger>
            <spec>H/5 * * * *</spec>
            <ignorePostCommitHooks>false</ignorePostCommitHooks>
        </hudson.triggers.SCMTrigger>
    </triggers>
    <concurrentBuild>false</concurrentBuild>
    <builders>
        <hudson.tasks.Shell">
            <command>
cd $WORKSPACE
mvn clean package -Pstandalone -DskipTests -Dmaven.repo.remote=https://maven.aliyun.com/repository/public
docker build -f docker/Dockerfile-backend -t docman-backend:$BUILD_NUMBER .
docker tag docman-backend:$BUILD_NUMBER localhost:5000/docman-backend:$BUILD_NUMBER
docker push localhost:5000/docman-backend:$BUILD_NUMBER
</command>
        </hudson.tasks.Shell>
    </builders>
    <publishers>
        <hudson.tasks.Mailer>
            <recipients>admin@example.com</recipients>
            <dontNotifyEveryUnstableBuild>false</dontNotifyEveryUnstableBuild>
            <sendToIndividuals>false</sendToIndividuals>
        </hudson.tasks.Mailer>
    </publishers>
    <buildWrappers/>
</project>
EOF
    echo "后端构建 Job XML 已生成: ${JOBS_DIR}/docman-backend-build.xml"
}

# Job 3: 前端构建
create_frontend_build_job() {
    echo -e "${GREEN}创建 Job: docman-frontend-build${NC}"

    cat > "${JOBS_DIR}/docman-frontend-build.xml" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <description>前端 Next.js 构建 + Docker 镜像</description>
    <keepDependencies>false</keepDependencies>
    <properties>
        <jenkins.model.BuildDiscarderProperty>
            <strategy class="hudson.tasks.LogRotator">
                <daysToKeep>30</daysToKeep>
                <numToKeep>50</numToKeep>
            </strategy>
        </jenkins.model.BuildDiscarderProperty>
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
    <blockBuildWhenDownstreamBuilding>false</blockBuildWhenUpstreamBuilding>
    <triggers>
        <hudson.triggers.SCMTrigger>
            <spec>H/10 * * * *</spec>
            <ignorePostCommitHooks>false</ignorePostCommitHooks>
        </hudson.triggers.SCMTrigger>
    </triggers>
    <concurrentBuild>false</concurrentBuild>
    <builders>
        <hudson.tasks.Shell">
            <command>
cd $WORKSPACE/docman-frontend
npm config set registry https://registry.npmmirror.com
npm install
npm run build
cd $WORKSPACE
docker build -f docker/Dockerfile-frontend -t docman-frontend:$BUILD_NUMBER .
docker tag docman-frontend:$BUILD_NUMBER localhost:5000/docman-frontend:$BUILD_NUMBER
docker push localhost:5000/docman-frontend:$BUILD_NUMBER
</command>
        </hudson.tasks.Shell>
    </builders>
    <publishers>
        <hudson.tasks.Mailer>
            <recipients>admin@example.com</recipients>
            <dontNotifyEveryUnstableBuild>false</dontNotifyEveryUnstableBuild>
            <sendToIndividuals>false</sendToIndividuals>
        </hudson.tasks.Mailer>
    </publishers>
    <buildWrappers/>
</project>
EOF
    echo "前端构建 Job XML 已生成: ${JOBS_DIR}/docman-frontend-build.xml"
}

# Job 4: 完整部署
create_deploy_job() {
    echo -e "${GREEN}创建 Job: docman-full-deploy${NC}"

    cat > "${JOBS_DIR}/docman-full-deploy.xml" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <description>完整部署流水线：中间件 + 后端 + 前端</description>
    <keepDependencies>false</keepDependencies>
    <properties>
        <jenkins.model.BuildDiscarderProperty>
            <strategy class="hudson.tasks.LogRotator">
                <daysToKeep>30</daysToKeep>
                <numToKeep>20</numToKeep>
            </strategy>
        </jenkins.model.BuildDiscarderProperty>
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
    <blockBuildWhenDownstreamBuilding>false</blockBuildWhenUpstreamBuilding>
    <triggers/>
    <concurrentBuild>false</concurrentBuild>
    <builders>
        <hudson.tasks.Shell">
            <command>
cd $WORKSPACE

# 1. 启动中间件
echo "=== 启动中间件 ==="
docker network create docman-network 2>/dev/null || true
docker-compose -f docker/docker-compose-standalone.yml up -d mysql redis elasticsearch minio

# 等待中间件就绪
echo "等待中间件启动..."
sleep 60

# 2. 构建后端
echo "=== 构建后端 ==="
mvn clean package -Pstandalone -DskipTests
docker build -f docker/Dockerfile-backend -t docman-backend:latest .

# 3. 构建前端
echo "=== 构建前端 ==="
cd docman-frontend
npm config set registry https://registry.npmmirror.com
npm install
npm run build
cd ..
docker build -f docker/Dockerfile-frontend -t docman-frontend:latest .

# 4. 启动应用
echo "=== 启动应用 ==="
docker-compose -f docker/docker-compose-standalone.yml up -d backend frontend

# 5. 健康检查
echo "=== 健康检查 ==="
sleep 30
curl -sf http://localhost:8080/actuator/health && echo "后端健康检查通过"
curl -sf http://localhost:3000/health && echo "前端健康检查通过"
</command>
        </hudson.tasks.Shell>
    </builders>
    <publishers>
        <hudson.tasks.Mailer>
            <recipients>admin@example.com</recipients>
            <dontNotifyEveryUnstableBuild>false</dontNotifyEveryUnstableBuild>
            <sendToIndividuals>false</sendToIndividuals>
        </hudson.tasks.Mailer>
        <htmlpublisher.HtmlPublisher>
            <reportTargets>
                <htmlpublisher.HtmlPublisherTarget>
                    <reportName>测试报告</reportName>
                    <htmlDir>target/surefire-reports</htmlDir>
                    <keepAll>false</keepAll>
                    <wrapperName>htmlwrapper</wrapperName>
                </htmlpublisher.HtmlPublisherTarget>
            </reportTargets>
        </htmlpublisher.HtmlPublisher>
    </publishers>
    <buildWrappers/>
</project>
EOF
    echo "完整部署 Job XML 已生成: ${JOBS_DIR}/docman-full-deploy.xml"
}

# Job 5: 冒烟测试
create_smoke_test_job() {
    echo -e "${GREEN}创建 Job: docman-smoke-test${NC}"

    cat > "${JOBS_DIR}/docman-smoke-test.xml" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <description>冒烟测试</description>
    <keepDependencies>false</keepDependencies>
    <properties>
        <jenkins.model.BuildDiscarderProperty>
            <strategy class="hudson.tasks.LogRotator">
                <daysToKeep>30</daysToKeep>
                <numToKeep>100</numToKeep>
            </strategy>
        </jenkins.model.BuildDiscarderProperty>
    </properties>
    <canRoam>true</canRoam>
    <disabled>false</disabled>
    <blockBuildWhenDownstreamBuilding>false</blockBuildWhenUpstreamBuilding>
    <triggers/>
    <concurrentBuild>false</concurrentBuild>
    <builders>
        <hudson.tasks.Shell">
            <command>
#!/bin/bash
GATEWAY_URL="${GATEWAY_URL:-http://192.168.145.252:9080}"
FRONTEND_URL="${FRONTEND_URL:-http://192.168.145.252:3000}"
REPORT_FILE="smoke-test-report.html"

echo "=== DocMan 冒烟测试 ==="
echo "后端地址: $GATEWAY_URL"
echo "前端地址: $FRONTEND_URL"
echo ""

# 初始化报告
cat > "$REPORT_FILE" << 'HTML_HEAD'
<!DOCTYPE html>
<html>
<head>
    <title>DocMan 冒烟测试报告</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .pass { color: green; font-weight: bold; }
        .fail { color: red; font-weight: bold; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #4CAF50; color: white; }
    </style>
</head>
<body>
    <h1>DocMan 冒烟测试报告</h1>
    <p>测试时间: TIMESTAMP</p>
    <table>
        <tr><th>测试项</th><th>状态</th><th>详情</th></tr>
HTML_HEAD

# 替换时间戳
sed -i "s/TIMESTAMP/$(date)/" "$REPORT_FILE"

# 测试函数
test_endpoint() {
    local name="$1"
    local url="$2"
    local expected_code="$3"

    if curl -sf -o /dev/null -w "%{http_code}" "$url" | grep -q "$expected_code"; then
        echo "<tr><td>$name</td><td class='pass'>PASS</td><td>HTTP $expected_code</td></tr>" >> "$REPORT_FILE"
        echo "✓ $name - PASS"
    else
        local actual_code=$(curl -sf -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000")
        echo "<tr><td>$name</td><td class='fail'>FAIL</td><td>期望: $expected_code, 实际: $actual_code</td></tr>" >> "$REPORT_FILE"
        echo "✗ $name - FAIL (期望: $expected_code, 实际: $actual_code)"
        return 1
    fi
}

# 执行测试
echo ""
echo "开始测试..."
echo ""

# 后端健康检查
test_endpoint "后端健康检查" "$GATEWAY_URL/actuator/health" "200"

# 前端健康检查
test_endpoint "前端健康检查" "$FRONTEND_URL/health" "200"

# API 测试
test_endpoint "用户服务健康" "$GATEWAY_URL/api/user/health" "200"
test_endpoint "文档服务健康" "$GATEWAY_URL/api/documents/health" "200"
test_endpoint "存储服务健康" "$GATEWAY_URL/api/storage/health" "200"
test_endpoint "搜索服务健康" "$GATEWAY_URL/api/search/health" "200"

# 结束报告
cat >> "$REPORT_FILE" << 'HTML_FOOT'
    </table>
    <h2>测试完成</h2>
</body>
</html>
HTML_FOOT

echo ""
echo "=== 测试完成 ==="
echo "报告已生成: $REPORT_FILE"

# 如果有测试失败，退出码为1
if grep -q "FAIL" "$REPORT_FILE"; then
    echo "存在失败的测试"
    exit 1
fi
</command>
        </hudson.tasks.Shell>
    </builders>
    <publishers>
        <htmlpublisher.HtmlPublisher>
            <reportTargets>
                <htmlpublisher.HtmlPublisherTarget>
                    <reportName>测试报告</reportName>
                    <htmlDir>.</htmlDir>
                    <keepAll>true</keepAll>
                    <wrapperName>htmlwrapper</wrapperName>
                </htmlpublisher.HtmlPublisherTarget>
            </reportTargets>
        </htmlpublisher.HtmlPublisher>
        <hudson.tasks.Mailer>
            <recipients>admin@example.com</recipients>
            <dontNotifyEveryUnstableBuild>true</dontNotifyEveryUnstableBuild>
            <sendToIndividuals>false</sendToIndividuals>
        </hudson.tasks.Mailer>
    </publishers>
    <buildWrappers/>
</project>
EOF
    echo "冒烟测试 Job XML 已生成: ${JOBS_DIR}/docman-smoke-test.xml"
}

# 主流程
main() {
    create_nexus_job
    create_backend_build_job
    create_frontend_build_job
    create_deploy_job
    create_smoke_test_job

    echo ""
    echo -e "${BLUE}=== 所有 Jobs 已创建 ===${NC}"
    echo ""
    echo "Job 列表:"
    echo "  1. docman-nexus-deploy    - Nexus 镜像仓库部署"
    echo "  2. docman-backend-build   - 后端 Maven + Docker 构建"
    echo "  3. docman-frontend-build  - 前端构建 + Docker 镜像"
    echo "  4. docman-full-deploy     - 完整部署流水线"
    echo "  5. docman-smoke-test      - 冒烟测试 + 报告"
    echo ""
    echo "XML 配置文件位置: ${JOBS_DIR}"
    echo ""
    echo "使用方法:"
    echo "  # 通过 Jenkins API 创建 jobs"
    echo "  curl -X POST ${JENKINS_URL}/createItem?name=docman-nexus-deploy \\"
    echo "    --user ${JENKINS_USER}:TOKEN \\"
    echo "    -H 'Content-Type: application/xml' \\"
    echo "    -d @${JOBS_DIR}/docman-nexus-deploy.xml"
    echo ""
}

main "$@"
