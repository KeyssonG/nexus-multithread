FROM eclipse-temurin:25-jdk-alpine as build
WORKDIR /app
COPY . .
RUN ./gradlew :modules:nexus:bootJar --no-daemon

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/modules/nexus/build/libs/nexus.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]
