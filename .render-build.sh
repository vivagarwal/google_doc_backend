#!/usr/bin/env bash
# Ensure JAVA_HOME is set and Maven is used correctly
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
./mvnw clean package
