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

Authentication requires a Base64-encoded JWT signing secret containing at least 32 bytes. Generate one for the current PowerShell session before starting the application:

```powershell
$secretBytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($secretBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)
```

The optional authentication settings and their defaults are:

| Variable | Default |
| --- | --- |
| `JWT_ISSUER` | `https://api.schedio.local` |
| `JWT_ACCESS_TOKEN_TTL` | `PT15M` |
| `JWT_REFRESH_TOKEN_TTL` | `P30D` |

See `.env.example` for the complete local configuration reference. Spring Boot reads environment variables directly; it does not load the example file automatically.

## API Foundation Endpoints

| Purpose | URL |
| --- | --- |
| Health check | `http://localhost:8080/actuator/health` |
| OpenAPI document | `http://localhost:8080/v3/api-docs` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |

Only the health Actuator endpoint is exposed, and health component details are hidden.

## Authentication

The backend uses a stateless Spring Security filter chain. The health check, OpenAPI document, Swagger UI, and authentication endpoints are public. All other endpoints require a valid JWT access token by default.

Form login, HTTP Basic authentication, server-side sessions, and the framework-generated development user are disabled. Authentication and authorization failures use the standard API error response with `401 Unauthorized` and `403 Forbidden` status codes.

Passwords are stored as BCrypt hashes. Access tokens are signed JWTs and default to a 15-minute lifetime. Refresh tokens default to 30 days, are stored only as SHA-256 hashes, and are rotated after every successful refresh. Reuse of an already rotated token revokes its active token family.

| Operation | Method and path | Successful response |
| --- | --- | --- |
| Log in | `POST /api/v1/auth/login` | `200 OK` with access token, refresh token, and user |
| Refresh | `POST /api/v1/auth/refresh` | `200 OK` with a rotated token pair |
| Log out | `POST /api/v1/auth/logout` | `204 No Content` |

Login accepts `email` and `password`. Refresh and logout accept `refreshToken`. Send access tokens to protected endpoints with `Authorization: Bearer <access-token>`.

Logout revokes the refresh-token family. An access token already issued remains valid until its short expiration time. Fresh databases do not yet contain users; user onboarding or a development bootstrap mechanism is the next required authentication-adjacent workflow.

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
