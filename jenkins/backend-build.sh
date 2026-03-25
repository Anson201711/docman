#!/bin/bash
set -e

echo "============================================"
echo "DocMan Backend Build"
echo "============================================"

# Install Maven if not available
if ! command -v mvn &> /dev/null; then
    echo "Maven not found, installing..."
    MAVEN_VERSION="3.9.6"
    curl -fsSL -o /tmp/apache-maven-${MAVEN_VERSION}-bin.zip \
        https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.zip
    unzip -q /tmp/apache-maven-${MAVEN_VERSION}-bin.zip -d /opt/
    ln -sf /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/local/bin/mvn
    rm -f /tmp/apache-maven-${MAVEN_VERSION}-bin.zip
    echo "Maven installed to /opt/apache-maven-${MAVEN_VERSION}"
fi

# Verify Maven
mvn --version

# Maven build with standalone profile
mvn clean package -Pstandalone -DskipTests

# Build Docker image
docker build -t docman-backend:standalone -f docker/Dockerfile-backend .

echo "============================================"
echo "Backend build completed!"
echo "Docker image: docman-backend:standalone"
echo "============================================"
