@echo off
cd /d "%~dp0.."
call gradlew.bat dockerBuildImage
if errorlevel 1 exit /b 1
start /b powershell -NoProfile -WindowStyle Hidden -Command ^
  "$swagger='http://localhost:8080/swagger-ui/index.html'; $h2='http://localhost:8080/h2-console';" ^
  "for ($i=0; $i -lt 40; $i++) {" ^
  "  try { Invoke-WebRequest $swagger -UseBasicParsing -TimeoutSec 2 | Out-Null; Start-Process $swagger; Start-Process $h2; exit 0 }" ^
  "  catch { Start-Sleep -Seconds 1 }" ^
  "}"
docker run --rm -p 8080:8080 parliamentary-voting-api:0.0.1-SNAPSHOT --spring.profiles.active=local
