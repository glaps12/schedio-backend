# Schedio Agent Instructions

## 1. Project Overview

Schedio is a multi-tenant appointment and business management platform.

It should support appointment-based businesses such as:

* Barbers
* Hair salons
* Beauty salons
* Spas
* Consultants
* Tutors
* Photography studios
* Veterinary clinics

Businesses should eventually be able to manage:

* Business profiles
* Employees
* Services
* Employee-service assignments
* Working hours
* Employee time off
* Customers
* Appointments
* Notifications
* Business reports

Schedio is a portfolio project intended to demonstrate strong full-stack development skills.

It should be realistic, polished, maintainable, and deployable without becoming unnecessarily enterprise-scale.

## 2. Repository Structure

Use two independent application repositories inside one local workspace.

The intended repository structure is:

```text
schedio-workspace/
├── AGENTS.md
├── PROGRESS.md
├── backend/                 # GitHub: glaps12/schedio-backend
│   ├── .git/
│   ├── AGENTS.md
│   ├── PROGRESS.md
│   └── ...
└── frontend/                # GitHub: glaps12/schedio-frontend
    ├── .git/
    ├── AGENTS.md
    ├── PROGRESS.md
    └── ...
```

Backend and frontend applications must remain separated inside their respective directories.

The `backend/` directory must contain only the backend repository, and the `frontend/` directory must contain only the frontend repository. Do not include one application inside the other repository and do not connect them as Git submodules.

The parent workspace is not a third application repository. Run Git commands from the relevant application directory.

The workspace-root `AGENTS.md` and `PROGRESS.md` files are canonical. The copies inside `backend/` and `frontend/` are context mirrors tracked in their respective repositories and must be kept synchronized with the root files whenever either canonical file changes.

## 3. Technology Stack

### Backend

* Java 21
* Spring Boot
* Maven
* Spring Security
* Spring Data JPA
* Bean Validation
* Flyway
* MySQL
* OpenAPI / Swagger
* JUnit
* Mockito
* Testcontainers

### Frontend

* Angular
* TypeScript
* Standalone components
* Angular Router
* Angular HttpClient
* Reactive Forms

### Infrastructure

* Docker
* Docker Compose
* GitHub Actions
* Environment variables

Use stable and mutually compatible versions.

Do not use:

* Snapshot versions
* Milestone versions
* Release candidate versions
* Beta versions
* Other prerelease versions

Do not add dependencies without a clear reason.

## 4. Backend Architecture

Use a modular monolith architecture.

Do not use microservices.

Organize backend code by feature and domain responsibility instead of creating global folders that contain every controller, service, repository, entity, and DTO.

Suggested backend modules:

* `auth`
* `user`
* `business`
* `employee`
* `customer`
* `servicecatalog`
* `availability`
* `appointment`
* `notification`
* `reporting`
* `audit`
* `shared`

Use `servicecatalog` instead of `service` as a package name to avoid confusion with classes that use the `Service` suffix.

Each module should own its domain responsibilities.

Avoid unnecessary dependencies and coupling between modules.

Business rules must not be implemented inside controllers.

Controllers should primarily:

* Receive HTTP requests
* Validate request input
* Call application services
* Return appropriate HTTP responses

Do not introduce abstraction layers without a clear benefit.

Prefer readable and maintainable code over clever or highly abstract code.

## 5. Multi-Tenancy

Use a shared database and shared schema multi-tenancy strategy.

Every tenant-owned record must be associated with a business identifier.

Tenant isolation must always be enforced by the backend.

A user belonging to one business must never be able to:

* Read another business's data
* Modify another business's data
* Delete another business's data
* Infer another business's data through identifiers
* Infer another business's data through error messages

Never rely only on frontend filtering, hidden buttons, or route guards for tenant security.

Tenant ownership must be checked for both read and write operations.

Repository queries should include tenant or business scope where appropriate.

Do not retrieve a resource globally and check its tenant only in the frontend.

## 6. Initial User Roles

Initial roles:

* `PLATFORM_ADMIN`
* `BUSINESS_OWNER`
* `EMPLOYEE`
* `CUSTOMER`

### PLATFORM_ADMIN

Manages platform-level operations.

Platform administrators must not automatically be treated as members of every tenant unless explicitly required by the use case.

### BUSINESS_OWNER

Manages only their own:

* Business profile
* Employees
* Services
* Employee-service assignments
* Working schedules
* Employee time off
* Customers
* Appointments
* Reports

### EMPLOYEE

Accesses their own:

* Profile
* Working schedule
* Time-off records
* Assigned appointments

### CUSTOMER

Accesses only their own:

* Profile
* Appointments
* Booking history

Authorization must always be enforced by the backend.

Frontend role checks are intended only for navigation and user experience. They must never be treated as sufficient security.

## 7. Appointment Domain Rules

Initial appointment statuses:

* `PENDING`
* `CONFIRMED`
* `COMPLETED`
* `CANCELLED`
* `NO_SHOW`

Initial appointment rules:

* An appointment cannot be created in the past.
* Appointment duration is determined by the selected service.
* The selected employee must be assigned to the selected service.
* The appointment must fit entirely within the employee's working hours.
* An appointment cannot overlap an employee's approved time off.
* An employee cannot have overlapping active appointments.
* Cancelled appointments do not block availability.
* A customer can access only their own appointments.
* An employee can access only appointments assigned to them unless explicitly authorized otherwise.
* A business owner can access only appointments belonging to their own business.
* Appointment status changes should be recorded in an appointment history table.
* Appointment creation must be concurrency-safe.
* Appointment rescheduling must be concurrency-safe.
* Appointment conflict prevention must be enforced by the backend.
* Appointment conflict prevention must not rely only on Angular validation.
* Validation and conflict checks must occur inside a transaction when necessary.
* Relevant database constraints should support application-level validation where possible.

Do not invent additional appointment rules until they are required by a task or explicitly approved.

## 8. Date, Time, and Timezone Strategy

Use a clear timezone strategy from the beginning.

* Store absolute timestamps in UTC.
* Each business should eventually have an IANA timezone value.
* Convert dates and times for display according to the business timezone.
* Use ISO-8601 values in API requests and responses.
* Use `Europe/Istanbul` as the initial local development and demo timezone unless changed later.
* Use timezone-aware Java date and time types where an absolute timestamp is required.
* Use appropriate local date and time types for recurring business schedules that are interpreted in the business timezone.

Do not store ambiguous local timestamps without timezone context where an absolute point in time is required.

## 9. Backend Development Rules

* Never expose JPA entities directly through API responses.
* Use separate request and response DTOs.
* Prefer manual DTO mapping initially.
* Introduce a mapper library only when it provides a clear benefit.
* Use Bean Validation for request validation.
* Use centralized exception handling.
* Use a consistent API error format.
* Use correct HTTP status codes.
* Use `BigDecimal` for monetary values.
* Define monetary currency explicitly when the related feature is implemented.
* Use transactions around business operations.
* Use optimistic or pessimistic locking where concurrency rules require it.
* Apply database constraints in addition to application-level validation.
* Manage every database schema change through Flyway.
* Do not use Hibernate schema generation as the production migration strategy.
* Do not use `ddl-auto=create`, `create-drop`, or `update` as the normal schema management approach.
* Do not store secrets or credentials in the repository.
* Use environment variables for configuration.
* Do not commit real `.env` files containing secrets.
* A safe example environment file may be added later when needed.
* Do not log passwords, access tokens, refresh tokens, or sensitive customer information.
* Avoid circular dependencies.
* Avoid unnecessary static utility classes.
* Follow SOLID principles pragmatically.
* Do not overengineer simple features.
* Do not introduce placeholder implementations and describe them as completed features.
* Prefer constructor injection.
* Avoid field injection.
* Keep domain and application rules outside persistence entities where practical.
* Use clear naming that reflects business meaning.

## 10. API Guidelines

Use versioned REST endpoints under:

```text
/api/v1
```

Use plural resource names where appropriate.

Examples:

```text
/api/v1/businesses
/api/v1/employees
/api/v1/services
/api/v1/customers
/api/v1/appointments
```

Potentially large collections should support:

* Pagination
* Filtering
* Sorting

Use a consistent pagination response structure throughout the application.

Use a consistent API error response containing at least:

* `timestamp`
* `status`
* `error`
* `message`
* `path`
* `validationErrors` when applicable

Never return:

* Stack traces
* Raw SQL errors
* Internal exception class names
* Database implementation details
* Sensitive implementation details

Use appropriate status codes, including:

* `200 OK` for successful reads and updates where appropriate
* `201 Created` for successful resource creation
* `204 No Content` for successful operations with no response body
* `400 Bad Request` for invalid input
* `401 Unauthorized` for missing or invalid authentication
* `403 Forbidden` for insufficient authorization
* `404 Not Found` for unavailable resources
* `409 Conflict` for state or scheduling conflicts

Do not reveal whether a resource belonging to another tenant exists when doing so would leak information.

## 11. Authentication and Security

Use Spring Security.

The intended authentication strategy is:

* Short-lived JWT access tokens
* Refresh tokens
* Secure password hashing
* Role-based authorization
* Refresh-token revocation
* Logout support

Never store plain-text passwords.

Never store raw refresh tokens when a secure hashed representation is sufficient.

Authentication should be implemented only after the basic backend foundation has been created and tested.

Security rules must eventually cover:

* Authentication
* Role authorization
* Tenant ownership
* Resource ownership
* Token expiration
* Token revocation
* Safe password handling

Do not create custom cryptographic algorithms.

## 12. Frontend Architecture

Use Angular standalone components.

Use a feature-based folder structure.

Intended structure:

```text
src/app/
├── core/
├── shared/
├── layouts/
└── features/
    ├── auth/
    ├── dashboard/
    ├── business/
    ├── employees/
    ├── services/
    ├── appointments/
    ├── customers/
    └── settings/
```

Frontend rules:

* Use route guards for authentication and role-based navigation.
* Use an HTTP interceptor for authentication tokens.
* Handle API errors consistently.
* Use Reactive Forms.
* Use typed models.
* Provide loading states.
* Provide empty states.
* Provide error states.
* Use responsive layouts.
* Keep components focused and reasonably small.
* Move genuinely reusable UI elements into shared components.
* Do not place complex business logic inside Angular templates.
* Separate API models from view models when their responsibilities differ.
* Do not use the frontend as the only security layer.
* Avoid a global state management library until the application complexity clearly requires one.
* Prefer Angular's built-in capabilities before introducing additional state libraries.
* Avoid duplicating backend business rules as authoritative frontend rules.
* Frontend validation should improve user experience, while backend validation remains authoritative.

## 13. Testing Strategy

Backend tests should include:

* Unit tests for important business rules
* Repository integration tests for non-trivial queries
* API integration tests for critical workflows
* Testcontainers for MySQL-dependent tests

The following are considered critical and must eventually be tested:

* Appointment availability calculation
* Appointment conflict detection
* Appointment concurrency behavior
* Tenant isolation
* Authorization rules
* Resource ownership rules
* Appointment status transitions

Frontend tests should prioritize:

* Important form validation
* Route guards
* API services
* Critical components
* Critical user workflows

Do not write meaningless tests only to increase coverage numbers.

Do not claim tests passed unless they were actually executed.

If a test cannot be executed because of an environment limitation, report that limitation clearly.

## 14. Documentation and Naming

All of the following must be written in English:

* Source code
* Class names
* Method names
* Variable names
* Package names
* Database objects
* API fields
* Technical documentation
* Commit messages

Explanations provided directly to the repository owner may be written in Turkish.

Update documentation whenever an architectural decision or important project behavior changes.

`AGENTS.md` should remain authoritative and relatively stable.

`PROGRESS.md` should be updated frequently and remain concise.

## 15. Agent Workflow

Before beginning any implementation task:

1. Read `AGENTS.md`.
2. Read `PROGRESS.md`.
3. Inspect the relevant existing files.
4. Identify the smallest complete change required.
5. Avoid modifying unrelated files.
6. Check whether the requested task conflicts with an existing project decision.

During implementation:

* Work in small and testable increments.
* Do not attempt to generate the entire application in one task.
* Do not silently change established architecture.
* Preserve working behavior unless the task requires changing it.
* Run relevant tests and checks.
* Fix errors introduced by the current change.
* Do not claim commands succeeded unless they were actually executed.
* Do not leave TODO comments unless the unfinished work is also recorded in `PROGRESS.md`.
* Do not overwrite unrelated user changes.
* Do not perform large refactors without a clear need.
* Do not continue into the next development phase unless explicitly requested.
* Keep each task limited to its requested scope.
* Ask for clarification only when a decision is genuinely blocking and cannot be resolved safely from the existing project rules.
* When possible, choose the simplest reasonable implementation that follows the established architecture.

After completing every meaningful task:

1. Run the relevant checks and tests.
2. Update `PROGRESS.md`.
3. Summarize the files changed.
4. State which commands and tests were run.
5. State their actual results.
6. Mention known limitations or unfinished work.
7. Recommend one logical next task.
8. Stop and wait for the next instruction instead of automatically implementing the recommendation.

## 16. PROGRESS.md Update Rules

After each meaningful task:

* Move completed items into the `Completed` section.
* Update the `Current Phase` when the project enters a new phase.
* Keep only currently active work in `In Progress`.
* Keep `Next Tasks` ordered by priority.
* Record important technical decisions under `Decisions`.
* Add unresolved but non-blocking matters under `Open Questions`.
* Add actual defects or limitations under `Known Issues`.
* Add a short dated entry to `Change Log`.
* Update the root `PROGRESS.md` first, then synchronize the `AGENTS.md` and `PROGRESS.md` context mirrors in `backend/` and `frontend/`.

Do not turn `PROGRESS.md` into a detailed daily diary.

Do not duplicate the full contents of `AGENTS.md` inside `PROGRESS.md`.

## 17. Scope Control

Do not add unrequested features simply because they may be useful.

The initial goal is a polished full-stack portfolio application, not an enterprise platform.

Do not initially introduce:

* Microservices
* Kubernetes
* Kafka
* RabbitMQ
* Event sourcing
* CQRS
* Elasticsearch
* Complex billing
* AI features
* Native mobile applications
* Distributed caching
* Multiple databases
* Third-party payment processing
* Complex subscription management

These technologies should be considered only after the core application is complete and there is a demonstrated need.
