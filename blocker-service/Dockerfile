FROM gradle:8.7.0-jdk21 AS build
WORKDIR /app
COPY gradle.properties settings.gradle build.gradle ./
COPY blocker-service blocker-service
COPY common common
RUN gradle :blocker-service:bootJar -x test

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY --from=build /app/blocker-service/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]