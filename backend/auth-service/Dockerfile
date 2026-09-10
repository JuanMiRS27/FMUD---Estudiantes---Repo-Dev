FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY backend/auth-service/pom.xml backend/auth-service/pom.xml
COPY backend/api-gateway/pom.xml backend/api-gateway/pom.xml
COPY backend/student-service/pom.xml backend/student-service/pom.xml
RUN chmod +x mvnw && ./mvnw -pl backend/auth-service -am dependency:go-offline

COPY backend backend
RUN ./mvnw -pl backend/auth-service -am clean package

FROM eclipse-temurin:21-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system fmud \
    && useradd --system --gid fmud --home-dir /app --shell /usr/sbin/nologin fmud
WORKDIR /app
COPY --from=build /workspace/backend/auth-service/target/auth-service-*.jar app.jar
RUN chown -R fmud:fmud /app
USER fmud
EXPOSE 8081
STOPSIGNAL SIGTERM
ENTRYPOINT ["java", "-jar", "app.jar"]
