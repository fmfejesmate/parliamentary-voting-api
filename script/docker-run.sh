#!/usr/bin/env sh
cd "$(dirname "$0")/.." || exit 1
./gradlew dockerBuildImage || exit 1
docker run --rm -p 8080:8080 parliamentary-voting-api:0.0.1-SNAPSHOT
