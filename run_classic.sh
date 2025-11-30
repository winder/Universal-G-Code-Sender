#!/usr/bin/env bash
./mvnw install
./mvnw exec:java -Dexec.mainClass="com.willwinder.universalgcodesender.MainWindow" -pl ugs-classic
