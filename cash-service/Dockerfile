FROM gradle:8.7.0-jdk21 AS build
WORKDIR /app
COPY gradle.properties settings.gradle build.gradle ./
COPY cash-service cash-service
COPY common common
RUN gradle :cash-service:bootJar -x test

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY --from=build /app/cash-service/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]