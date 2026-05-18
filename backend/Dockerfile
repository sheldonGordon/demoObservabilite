FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml /workspace/pom.xml
COPY backend/pom.xml /workspace/backend/pom.xml
RUN mvn -pl backend -am -DskipTests dependency:go-offline

COPY backend/src /workspace/backend/src
RUN mvn -pl backend -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN mkdir -p /otel
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.8.0/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar

COPY --from=build /workspace/backend/target/backend-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

