# Simple Invoice Platform
Simple Invoice Platform is a RESTful backend application for managing customers, products and invoices.

The application models the core lifecycle of an invoice, including immutable invoice snapshots, controlled status transitions, stateless JWT-based authentication and role-based authorization. It is designed as a focused backend project rather than a complete accounting or payment-processing system.

## Features
* customer and product management
* invoice creation from existing customers and products
* historical snapshots of customer and product data within invoices
* automatic invoice total calculation
* controlled invoice status transitions
* JWT-based authentication
* role-based authorization (`USER`, `ADMIN`)
* unique username enforcement
* request validation and centralized error handling
* openAPI documentation with Swagger UI
* unit, controller, security and integration tests

## Tech Stack
* Java 17
* Spring Boot 4
* Spring Web MVC
* Spring Data MongoDB
* Spring Security
* MongoDB
* JWT (JJWT)
* Springdoc OpenAPI / Swagger UI
* JUnit
* Mockito
* Maven
* Lombok

## Domain Overview
The application manages customers, products and invoices.

Invoices contain snapshots of the customer name and relevant product information at the time of creation. This ensures that historical invoices remain unchanged if a customer or product is later updated or deleted.

Invoice lines are embedded within their parent invoice and have no independent lifecycle.

The normal invoice lifecycle is: `CREATED -> VALIDATED -> SENT -> PAID`

Invoices may also be canceled while in the `CREATED`, `VALIDATED` or `SENT` state. `PAID` and `CANCELED` are terminal states.

More information about the domain and architectural decisions can be found in the design document at [`docs/DESIGN.md`](docs/DESIGN.md).

## Security
The API uses username/password authentication and JWT bearer tokens.

Successful authentication returns a JWT which is supplied with subsequent protected requests.

JWTs identify users by username. Current roles and authorities are loaded from the database for each authenticated request.

The application supports two roles:
* `USER`
* `ADMIN`

Passwords are stored in encoded form, and usernames are protected by a unique MongoDB index.

On application startup, if no `ADMIN` user already exists, an initial administrator account is created using credentials supplied through environment variables.

Security is stateless, and requests that do not match an explicitly permitted authorization rule are denied by default.

### Authorization
Access to API operations is role-based:

| Operation                             | Access          |
| ------------------------------------- | --------------- |
| Login                                 | Public          |
| User registration                     | Public          |
| View users                            | `ADMIN`         |
| Read customers, products and invoices | `USER`, `ADMIN` |
| Create, update or delete resources    | `ADMIN`         |
| Swagger UI / OpenAPI documentation    | Public          |

All other requests are denied by default.

## Configuration
The application configuration can be found in:
```text
src/main/resources/application.properties
```

By default, the application expects MongoDB to be available at:
```text
localhost:27017
```

Also by default, it uses the following database:
```text
invoice-platform
```

The following environment variables must be supplied before starting the application:
```text
INVOICE_ADMIN_USERNAME
INVOICE_ADMIN_PASSWORD
INVOICE_JWT_SECRET
```

`INVOICE_JWT_SECRET` is used to sign JWTs using HS256 and must contain a suitable secret key.

## Running the Application
### Prerequisites
The following must be installed and available:
* Java 17
* Maven
* MongoDB

Set the required environment variables, then start the application with:
```bash
mvn spring-boot:run
```

Alternatively, the application can be started through an IDE after configuring the same environment variables.

## API Documentation
With the application running, interactive API documentation is available through the Swagger UI.
```text
/swagger-ui/index.html
```

The generated OpenAPI specification is available at:
```text
/v3/api-docs
```

For a local application using the default values, these resolve to `http://localhost:8080/swagger-ui/index.html` and `http://localhost:8080/v3/api-docs` respectively.

The Swagger/OpenAPI endpoints are intended for development and API exploration.

To call protected endpoints from Swagger UI, first authenticate with `POST /api/auth/login`, copy the returned JWT, click Authorize, and enter the token in the bearer authentication field.

## Testing
Run the complete test suite with:
```bash
mvn test
```

Tests use the `test` Spring profile and a separate MongoDB database. The test configuration is found at:
```text
src/test/resources/application-test.properties
```

Test authentication credentials and the test JWT secret are deliberately isolated from the normal application configuration.

The test suite covers service behavior, controller behavior, authentication and authorization, JWT handling, invoice status transitions, validation, exception handling and security integration.

## Project Scope
This project intentionally focuses on the backend domain and API design of a simple invoicing system. Additional features such as payment processing are outside the scope of the current version.

See the design document at [`docs/DESIGN.md`](docs/DESIGN.md) for more detailed design decisions and domain rules.
