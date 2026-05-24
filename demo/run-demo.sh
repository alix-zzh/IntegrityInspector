#!/usr/bin/env bash
set -e

echo "Building demo bundle..."
mvn clean package -q
echo "Running demo: single project against baselines"
java -jar target/integrity-inspector-0.9.0.jar \
  --config demo/config.sample.json \
  --checking-project demo_projects/check/ProjCheck \
  --baseline-projects demo_projects/baseline
