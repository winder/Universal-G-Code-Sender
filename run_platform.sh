#!/usr/bin/env bash
./mvnw install -DskipTests=true && \
./mvnw nbm:run-platform -pl ugs-platform/application
