#!/usr/bin/env sh
cd "$(dirname "$0")/.." || exit 1
./gradlew bootRun --args="--spring.profiles.active=local"
