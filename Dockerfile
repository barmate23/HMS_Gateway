# syntax=docker/dockerfile:1

# ======================
# Build stage
# ======================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build
COPY src src
RUN mvn clean package -DskipTests

# ======================
# Run stage
# ======================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 9093
ENTRYPOINT ["java", "-jar", "app.jar"]
