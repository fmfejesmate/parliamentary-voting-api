@echo off
cd /d "%~dp0.."
call gradlew.bat bootRun --args="--spring.profiles.active=local"
