FROM sbtscala/scala-sbt:eclipse-temurin-alpine-21.0.7_6_1.11.3_3.7.1 as builder

WORKDIR /app

# Copy build definition files
COPY build.sbt ./
COPY project ./project/

# Copy source code
COPY src ./src/

# Build the application
RUN sbt clean compile stage

# Create runtime image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built application
COPY --from=builder /app/target/universal/stage/lib /app/lib
COPY --from=builder /app/target/universal/stage/bin /app/bin

# Set executable permissions
RUN chmod +x /app/bin/res-mgr-server

# Expose the application port
EXPOSE 8080

# Set environment variables with default values (can be overridden at runtime)
ENV PORT=8080 \
    DB_HOST=localhost \
    DB_PORT=5432 \
    DB_NAME=resource_manager \
    DB_USER=postgres \
    DB_PASSWORD=mysecretpassword

# Run the application
ENTRYPOINT ["/app/bin/res-mgr-server"]