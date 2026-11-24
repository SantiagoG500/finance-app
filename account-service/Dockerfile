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

# Cache Maven dependencies offline
RUN mvn dependency:go-offline -B

# Copy account-service module source code
COPY account-service/ account-service/

# Package the account-service-boot module and its required dependencies
RUN mvn clean package -DskipTests -pl :account-service-boot -am

# ==========================================
# Runtime Stage
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the packaged Spring Boot executable jar
COPY --from=build /app/account-service/account-service-boot/target/account-service-boot-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
