#!/usr/bin/env bash
mvn install -DskipTests=true && \
mvn nbm:run-platform -pl ugs-platform/application
