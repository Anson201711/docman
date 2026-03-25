#!/bin/sh
# Maven Wrapper script - downloads Maven automatically if not available

set -e

# Determine project base dir
PRG="$0"
while [ -h "$PRG" ]; do
    ls=$(ls -ld "$PRG")
    link=$(expr "$ls" : '.*-> \(.*\)$')
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=$(dirname "$PRG")/"$link"
    fi
done
SAVED="$(pwd)"
cd "$(dirname "$PRG")/" > /dev/null || exit 1
APP_HOME="$(pwd -P)"
cd "$SAVED" > /dev/null || exit 1

# Download Maven if needed
if ! command -v mvn > /dev/null 2>&1; then
    echo "Maven not found locally, downloading..."
    MAVEN_VERSION="3.9.6"
    MAVEN_DIST="apache-maven-${MAVEN_VERSION}-bin.zip"
    MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/${MAVEN_DIST}"
    MAVEN_HOME="/tmp/apache-maven-${MAVEN_VERSION}"

    if [ ! -d "$MAVEN_HOME" ]; then
        echo "Downloading Maven ${MAVEN_VERSION}..."
        curl -fsSL -o "/tmp/${MAVEN_DIST}" "$MAVEN_URL"
        unzip -q "/tmp/${MAVEN_DIST}" -d /tmp/
        rm -f "/tmp/${MAVEN_DIST}"
    fi

    export MAVEN_HOME
    export PATH="$MAVEN_HOME/bin:$PATH"
fi

exec mvn "$@"
