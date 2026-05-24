@echo off
echo Building demo bundle...
call mvn -q -e package
if errorlevel 1 exit /b %errorlevel%
echo Running demo: single project against baselines
java -jar target\integrity-inspector-0.9.0.jar --config demo\config.sample.json --checking-project demo_projects\check\ProjCheck --baseline-projects demo_projects\baseline
