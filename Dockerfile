FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY parliamentary-voting-api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
