#!/usr/bin/env bash
set -e

echo "Building demo bundle..."
mvn clean package -q
echo "Running demo: checking-directory and config.json"
java -jar target/integrity-inspector-0.9.0-jar-with-dependencies.jar --config demo/config.sample.json --checking-directory demo_projects/check
