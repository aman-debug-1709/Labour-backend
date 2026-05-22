# Build stage - use JDK 21 base and the project's own Gradle wrapper
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
# Copy Gradle wrapper and config first (better Docker layer caching)
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
RUN chmod +x gradlew
# Download dependencies first (cached layer)
RUN ./gradlew dependencies --no-daemon || true
# Copy source and build
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Package stage - lightweight JRE only
FROM eclipse-temurin:21-jre-jammy
EXPOSE 8080
RUN mkdir /app
COPY --from=build /app/build/libs/labour-backend-0.0.1-SNAPSHOT.jar /app/spring-boot-application.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/spring-boot-application.jar"]
