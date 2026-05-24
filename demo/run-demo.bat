@echo off
echo Building demo bundle...
mvn -q -e package
echo Running demo: checking-directory and config.json
java -jar target\integrity-inspector-0.9.0-jar-with-dependencies.jar --config demo\config.sample.json --checking-directory demo_projects/check
