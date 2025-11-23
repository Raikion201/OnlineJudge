# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage - includes compilers and runtime environments
FROM eclipse-temurin:21-jre-alpine

# Install required tools for code execution
RUN apk add --no-cache \
    bash \
    gcc \
    g++ \
    python3 \
    nodejs \
    npm \
    go \
    openjdk17

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Create work directory
RUN mkdir -p /tmp/code-executor && chmod 777 /tmp/code-executor

EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]

