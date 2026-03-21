#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

# ── Pre-flight checks ──────────────────────────────────────────────
if ! command -v java &>/dev/null; then
    echo "ERROR: Java not found. Install JDK 17+ and make sure 'java' is on your PATH."
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | head -1 | sed 's/.*"\([0-9]*\).*/\1/')
if [ "$JAVA_VER" -lt 11 ] 2>/dev/null; then
    echo "WARNING: Java 11+ required (detected version $JAVA_VER). The build may fail."
fi

# ── Ensure Gradle wrapper JAR exists ────────────────────────────────
WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
WRAPPER_URL="https://services.gradle.org/distributions/gradle-8.5-bin.zip"
JAR_URL="https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ] || [ "$(wc -c < "$WRAPPER_JAR")" -lt 10000 ]; then
    echo "Gradle wrapper JAR missing or invalid. Downloading..."
    mkdir -p gradle/wrapper
    if command -v curl &>/dev/null; then
        curl -fsSL -o "$WRAPPER_JAR" "$JAR_URL"
    elif command -v wget &>/dev/null; then
        wget -q -O "$WRAPPER_JAR" "$JAR_URL"
    else
        echo "ERROR: Neither curl nor wget found. Install one to download the Gradle wrapper."
        echo "Alternatively, run: gradle wrapper --gradle-version 8.5"
        exit 1
    fi
    echo "Gradle wrapper downloaded."
fi

# ── Build & run ─────────────────────────────────────────────────────
echo "Building and launching Rogue Forge..."
if [[ "$OSTYPE" == "darwin"* ]]; then
    ./gradlew :lwjgl3:run -PmacJvmArgs=-XstartOnFirstThread "$@"
else
    ./gradlew :lwjgl3:run "$@"
fi
