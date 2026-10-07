@echo off
cd /d "%~dp0.."
call gradlew.bat dockerBuildImage
if errorlevel 1 exit /b 1
docker run --rm -p 8080:8080 parliamentary-voting-api:0.0.1-SNAPSHOT
