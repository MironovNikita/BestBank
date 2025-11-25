#Build
FROM gradle:8.14.3-jdk-21-and-24 AS buildstage
WORKDIR /app
COPY . .
RUN gradle clean build -x test

#Runtime
FROM openjdk:21
WORKDIR /app
COPY --from=buildstage /app/transfers-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]