FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml /workspace/pom.xml
COPY batch/pom.xml /workspace/batch/pom.xml
RUN mvn -pl batch -am -DskipTests dependency:go-offline

COPY batch/src /workspace/batch/src
RUN mvn -pl batch -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN mkdir -p /otel
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.8.0/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar

COPY --from=build /workspace/batch/target/batch-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

