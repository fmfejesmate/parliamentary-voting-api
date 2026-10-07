#!/usr/bin/env sh
cd "$(dirname "$0")/.." || exit 1
./gradlew dockerBuildImage || exit 1
(
	swagger="http://localhost:8080/swagger-ui/index.html"
	h2="http://localhost:8080/h2-console"
	i=0
	while [ "$i" -lt 40 ]; do
		if curl -sf -o /dev/null "$swagger"; then
			if command -v xdg-open >/dev/null 2>&1; then
				xdg-open "$swagger"
				xdg-open "$h2"
			elif command -v open >/dev/null 2>&1; then
				open "$swagger"
				open "$h2"
			fi
			exit 0
		fi
		i=$((i + 1))
		sleep 1
	done
) &
docker run --rm -p 8080:8080 parliamentary-voting-api:0.0.1-SNAPSHOT --spring.profiles.active=local
