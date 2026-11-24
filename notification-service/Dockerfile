# syntax=docker/dockerfile:1
# ==========================================
# Build Stage
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy root and module POM files to leverage Docker layer caching
COPY pom.xml .
COPY account-service/pom.xml account-service/
COPY account-service/account-service-domain/pom.xml account-service/account-service-domain/
COPY account-service/account-service-infrastructure/pom.xml account-service/account-service-infrastructure/
COPY account-service/account-service-boot/pom.xml account-service/account-service-boot/

COPY transaction-service/pom.xml transaction-service/
COPY transaction-service/transaction-service-domain/pom.xml transaction-service/transaction-service-domain/
COPY transaction-service/transaction-service-infrastructure/pom.xml transaction-service/transaction-service-infrastructure/
COPY transaction-service/transaction-service-boot/pom.xml transaction-service/transaction-service-boot/

COPY notification-service/pom.xml notification-service/
COPY notification-service/notification-service-domain/pom.xml notification-service/notification-service-domain/
COPY notification-service/notification-service-infrastructure/pom.xml notification-service/notification-service-infrastructure/
COPY notification-service/notification-service-boot/pom.xml notification-service/notification-service-boot/

# Copy notification-service module source code
COPY notification-service/ notification-service/

# Package the notification-service-boot module and its required dependencies
RUN --mount=type=cache,id=m2-cache,target=/root/.m2,sharing=locked \
    mvn clean package -DskipTests -pl :notification-service-boot -am -B

# ==========================================
# Runtime Stage
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the packaged Spring Boot executable jar
COPY --from=build /app/notification-service/notification-service-boot/target/notification-service-boot-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
