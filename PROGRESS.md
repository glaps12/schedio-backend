# Schedio Development Progress

## Project Status

```text
Foundation
```

## Current Phase

```text
Authentication foundation
```

## Completed

* Project concept selected
* Project name selected: Schedio
* Main technology stack selected
* Separate backend and frontend repository structure selected
* Modular monolith backend architecture selected
* Shared database and shared schema multi-tenancy selected
* Initial user roles identified
* Initial appointment rules identified
* Initial timezone strategy identified
* `AGENTS.md` created
* `PROGRESS.md` created
* Spring Boot backend initialized with Java 21, Maven Wrapper, Spring Boot 4.1.0, and Spring Web MVC
* Angular frontend initialized with the Angular 21.2 release line, standalone components, routing, and SCSS
* Root project guidance mirrored into `backend/` and `frontend/`
* Initial backend context-load test passed
* Initial frontend production build and component tests passed
* GitHub CLI authenticated for the `glaps12` account
* Public `schedio-backend` and `schedio-frontend` GitHub repositories created
* Initial backend and frontend commits pushed to their respective `main` branches
* Backend persistence dependencies added for Spring Data JPA, Bean Validation, Flyway, MySQL, and Testcontainers
* Backend datasource configured through environment variables with safe local defaults
* Local MySQL 8.4.11 service configured with Docker Compose on host port 3307
* Flyway enabled as the database migration mechanism
* Hibernate schema generation restricted to validation
* First MySQL integration test added and passed with Testcontainers
* Maven Wrapper Windows bootstrap null-target handling fixed
* Initial modular backend package structure created with documented feature boundaries
* First Flyway migration added for the `businesses` table and verified against MySQL
* Initial `Business` JPA entity and Spring Data repository added and verified against MySQL
* Standard API error and field-validation response models added
* Global exception handling added for validation, malformed requests, missing resources, state conflicts, and unexpected errors
* OpenAPI 3 and Swagger UI configured with Springdoc
* Actuator health endpoint configured with details hidden
* Backend local setup, execution, endpoint, and test commands documented
* API foundation verified with automated tests and live HTTP checks

## In Progress

* Preparing the authentication and security foundation

## Next Tasks

1. Implement the authentication and authorization foundation.
2. Add the first secured Business API workflows.
3. Begin Angular core and business feature integration.

## Decisions

* Project name: Schedio
* Repository style: Separate backend and frontend repositories
* Backend repository: `glaps12/schedio-backend`
* Frontend repository: `glaps12/schedio-frontend`
* Backend architecture: Modular monolith
* Backend module roots: `auth`, `user`, `business`, `employee`, `customer`, `servicecatalog`, `availability`, `appointment`, `notification`, `reporting`, `audit`, and `shared`
* Module subpackages will be introduced with working feature code instead of creating empty technical layers up front
* Multi-tenancy strategy: Shared database and shared schema
* Database: MySQL
* Local and test MySQL version: 8.4.11
* Local Docker MySQL host port: 3307
* Initial business identifiers: Database-generated `BIGINT` values
* Business records require a name and IANA timezone; creation and update timestamps are database-managed in UTC
* Database-managed business timestamps are mapped as read-only JPA fields
* Backend language: Java 21
* Backend build tool: Maven
* Spring Boot version: 4.1.0
* Frontend framework: Angular
* Angular release line: 21.2.x, selected for compatibility with the local Node.js 22.20.0 runtime
* Angular component model: Standalone components
* Frontend routing: Angular Router
* Frontend styling: SCSS
* API style: Versioned REST API
* API error responses: Consistent timestamp, status, error, message, path, and validation error fields
* API documentation: Springdoc OpenAPI 3.1.0 with Swagger UI at `/swagger-ui.html`
* Operational health endpoint: Spring Boot Actuator at `/actuator/health` with component details hidden
* Authentication plan: JWT access tokens and refresh tokens
* Database migration tool: Flyway
* Hibernate schema strategy: Validate only
* Database integration testing: Testcontainers 2.0.5 with ephemeral MySQL containers
* Local infrastructure: Docker Compose
* Timestamp storage: UTC
* Initial local and demo timezone: Europe/Istanbul
* Technical naming and documentation language: English
* Canonical project guidance location: Parent workspace root
* Application-directory guidance files: Synchronized context mirrors
* Development workflow: Keep `main` stable and implement new tasks on task-specific branches

## Open Questions

Resolve these questions only when the related feature is being designed.

They must not block the initial project foundation.

* Must customers create an account before booking?
* Can businesses allow guest bookings?
* Can businesses configure cancellation windows?
* Is appointment confirmation automatic or manual?
* Can one appointment contain multiple services?
* Can multiple employees participate in one appointment?
* Can working hours vary for a specific date?
* Will email notifications be included in the first release?
* Will businesses have public booking pages with unique slugs?
* Which currency or currencies will the first release support?

## Known Issues

* None yet

## Change Log

### 2026-08-05

* Created the initial repository guidance.
* Defined the Schedio project architecture and scope.
* Defined the initial development rules.
* Defined the initial progress tracking structure.
* Initialized the Spring Boot backend and Angular frontend application skeletons.
* Added Maven and npm dependency lock/wrapper support generated by the official project tools.
* Verified the generated backend and frontend projects with their initial tests and frontend production build.
* Added synchronized project-guidance mirrors for both application directories.

### 2026-08-08

* Changed version control from a monorepo to separate backend and frontend repositories.
* Created public `schedio-backend` and `schedio-frontend` repositories on GitHub.
* Committed and pushed each application only to its corresponding repository.

### 2026-08-13

* Added the backend persistence, validation, migration, MySQL, and Testcontainers dependencies.
* Added environment-based datasource configuration and disabled Hibernate schema mutation.
* Added and started a healthy Docker Compose MySQL service on host port 3307.
* Added and passed the first real MySQL integration test with Flyway verification.
* Fixed Maven Wrapper startup on Windows when the Maven user directory is not a filesystem link.
* Created and documented the initial feature-based backend module packages.
* Verified the package structure with the full backend Maven test run.

### 2026-08-15

* Pushed the backend persistence and modular package foundation to GitHub.
* Adopted task-specific branches for new development work.
* Added Flyway V1 to create the initial `businesses` table.
* Added MySQL integration coverage for the migration, required values, and audit timestamps.
* Added and verified the initial `Business` entity and `BusinessRepository` persistence mapping.

### 2026-08-25

* Added the shared API error contract and centralized exception handling.
* Added and verified OpenAPI documentation, Swagger UI, and the Actuator health endpoint.
* Documented backend local setup, execution, endpoint, and test commands.
* Completed the basic backend and API foundation and moved to the authentication foundation phase.
