# Stage 1: Build the application using Maven
FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean install

# Stage 2: Create the final, smaller image to run the application
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/cloudshareapi-0.0.1-SNAPSHOT.jar .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "cloudshareapi-0.0.1-SNAPSHOT.jar"]