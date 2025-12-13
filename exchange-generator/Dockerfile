FROM gradle:8.7.0-jdk21 AS build
WORKDIR /app
COPY gradle.properties settings.gradle build.gradle ./
COPY exchange-generator exchange-generator
COPY common common
RUN gradle :exchange-generator:bootJar -x test

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY --from=build /app/exchange-generator/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]