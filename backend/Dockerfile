# syntax=docker/dockerfile:1.7
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests -e package

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache wget
WORKDIR /app
COPY --from=build /workspace/target/sifap-backend-*.jar /app/app.jar
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q UP || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
