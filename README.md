# Schedio Backend

Schedio is a multi-tenant appointment and business management platform built with Java 21, Spring Boot, MySQL, and Flyway.

## Prerequisites

* Java 21
* Docker Desktop with Docker Compose

## Run Locally

Start the local MySQL service:

```powershell
docker compose up -d
```

Start the backend on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

Flyway applies pending database migrations automatically when the application starts.

The local datasource defaults are safe development values and can be overridden with environment variables:

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3307/schedio` |
| `DB_USERNAME` | `schedio` |
| `DB_PASSWORD` | `schedio_local_password` |

## API Foundation Endpoints

| Purpose | URL |
| --- | --- |
| Health check | `http://localhost:8080/actuator/health` |
| OpenAPI document | `http://localhost:8080/v3/api-docs` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |

Only the health Actuator endpoint is exposed, and health component details are hidden.

## Tests

Run all backend tests on Windows:

```powershell
.\mvnw.cmd test
```

On macOS or Linux:

```bash
./mvnw test
```

The integration tests use Testcontainers and require Docker to be running.

## Stop Local Infrastructure

Stop the local MySQL container while preserving its Docker volume:

```powershell
docker compose stop
```
