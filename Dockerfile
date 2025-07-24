# Stage 1: Build the application
FROM gradle:8.4-jdk17 AS builder
WORKDIR /app

# Copy all files and cache dependencies
COPY . .
RUN gradle clean installDist

# Stage 2: Create a lightweight runtime image
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy built artifacts from builder stage
COPY --from=builder /app/build/install/benford-api /app

# Expose port (optional, based on your application.conf)
EXPOSE 8080

# Default command to run the application
CMD ["bin/benford-api"]
