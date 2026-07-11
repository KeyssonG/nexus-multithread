FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
COPY libs libs

COPY modules/empresa modules/empresa
COPY modules/estoque modules/estoque
COPY modules/administracao modules/administracao
COPY modules/validacao modules/validacao
COPY modules/validacao-ad modules/validacao-ad
COPY modules/nexus modules/nexus

RUN chmod +x gradlew && sed -i 's/\r$//' gradlew && ./gradlew :modules:nexus:bootJar --no-daemon && \
    rm -rf ~/.gradle/caches/

FROM eclipse-temurin:25-jre-alpine
RUN apk add --no-cache tzdata curl && \
    addgroup -S nexus && \
    adduser -S nexus -G nexus

WORKDIR /app

COPY --from=build /app/modules/nexus/build/libs/nexus.jar app.jar

EXPOSE 8085

USER nexus

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl --fail http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]
