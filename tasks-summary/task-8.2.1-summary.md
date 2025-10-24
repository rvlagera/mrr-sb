# Sub-task 8.2.1: Create Dockerfile

**Completed:** 2025-10-24 11:40

## Description

Implemented a production-ready Dockerfile that builds the Spring Boot Kotlin application using Maven in a dedicated builder stage and runs it on a lightweight Corretto 17 Alpine base image with a health check targeting the actuator endpoint.

## Key Accomplishments

1. **Builder Stage**
   - Based on `amazoncorretto:17-alpine`
   - Copies Maven wrapper assets and resolves dependencies with `dependency:go-offline`
   - Compiles the project and packages `mrr-sb-0.0.1-SNAPSHOT.jar` with tests skipped

2. **Runtime Stage**
   - Installs `curl` for health checks
   - Copies the built jar as `app.jar` and exposes port `8080`
   - Adds a health check probing `http://localhost:8080/api/actuator/health`
   - Starts the service via `ENTRYPOINT ["java", "-jar", "app.jar"]`

## Files Created

- `Dockerfile`

## Files Modified

- `plan_sb.md` – Marked Sub-task 8.2.1 as completed
- `tasks-summary.md` – Added summary for this sub-task

## Usage Notes

- Build image: `docker build -t mrr-sb:latest .`
- Run container: `docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=docker -e DB_PASSWORD=... -e JWT_SECRET=... mrr-sb:latest`
- Health check expects actuator endpoint available at `/api/actuator/health`
- Update `COPY --from=builder` path if project version changes from `0.0.1-SNAPSHOT`

## Verification

- No automated Docker build executed in this environment. Recommend running `docker build` locally when Docker is available.

