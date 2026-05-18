# ============================================================
# Stage 1: Build (uses Maven from the image — no mvnw needed)
# ============================================================
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy pom first for layer caching of dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

# Copy source and build
COPY src ./src
RUN mvn package -DskipTests -B -q

# ============================================================
# Stage 2: Runtime (minimal JRE image)
# ============================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

# Create non-root user for security
RUN addgroup -S cimhans && adduser -S cimhans -G cimhans
USER cimhans

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
