# Путь: ./stats-service/stats-server/Dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Используем маску *, чтобы не зависеть от версии в названии jar
COPY target/stats-server-*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
