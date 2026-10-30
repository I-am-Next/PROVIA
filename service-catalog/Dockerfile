# ---- BUILDER ----
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
# Copy root POM
COPY pom.xml /app/
COPY common-events /app/common-events
COPY service-catalog /app/service-catalog
COPY booking-service /app/booking-service
COPY auth-service /app/auth-service
COPY search-service /app/search-service
# Build only the service-catalog module (and dependencies)
RUN mvn -pl service-catalog -am clean package -DskipTests
# ---- RUNTIME ----
FROM eclipse-temurin:21-jdk
WORKDIR /app
# Copy built JAR from builder stage
COPY --from=builder /app/service-catalog/target/*.jar app.jar
# Default command
ENTRYPOINT ["java", "-jar", "app.jar"]