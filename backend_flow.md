# Backend Architecture and Flow Documentation

This document provides a comprehensive overview of the backend application flow, detailing how incoming requests are processed, how security is enforced, and how data moves between different layers of the application.

## 1. Architecture Overview

The backend is built with **Spring Boot** using a standard layered architecture:

- **Controllers (`devcourses.backvue.back.controller`)**: The entry points for HTTP requests. They parse incoming data (DTOs) and return HTTP responses (`ResponseEntity`).
- **Services (`devcourses.backvue.back.service`)**: Contain the core business logic. They orchestrate operations and interact with the data layer.
- **Repositories (`devcourses.backvue.back.repository`)**: Interfaces extending Spring Data `JpaRepository` that handle database interactions (CRUD operations).
- **Models (`devcourses.backvue.back.model`)**: JPA Entities representing database tables (e.g., `User`).
- **Security (`devcourses.backvue.back.security`)**: Spring Security configuration, JWT token generation/validation, and custom filters.
- **DTOs (`devcourses.backvue.back.dto`)**: Data Transfer Objects used to receive data from the frontend (e.g., `LoginRequest`, `SignUpRequest`).

## 2. Security Flow (Spring Security & JWT)

The application secures its endpoints using **JSON Web Tokens (JWT)** and a stateless session management policy, meaning no user sessions are stored on the server.

### 2.1 Security Configuration (`SecurityConfig.java`)

- **CORS**: Configured to allow requests from `http://localhost:3000` and `http://localhost:5173`. Methods permitted: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`.
- **CSRF**: Disabled (safe for stateless APIs).
- **Public Endpoints**: `/api/signin`, `/api/signup`, `/api/health`, and all HTTP `OPTIONS` requests (pre-flight checks).
- **Protected Endpoints**: `/api/users/**` (Requires `ADMIN` role). All other endpoints require the user to be authenticated.
- **Exception Handling**: Unauthenticated access attempts return an HTTP `401 Unauthorized` status.
- **Filter Chain**: The custom `JwtAuthenticationFilter` is inserted _before_ the standard `UsernamePasswordAuthenticationFilter`.

### 2.2 JWT Request Interception (`JwtAuthenticationFilter.java`)

Whenever a request is made to a protected endpoint:

1.  The filter intercepts the request.
2.  It looks for an `Authorization` header starting with `Bearer `.
3.  If found, it extracts the token and uses `JwtTokenProvider` to validate it.
4.  If valid, it extracts the `username` (email) from the token.
5.  It loads the `UserDetails` from the database.
6.  It creates a `UsernamePasswordAuthenticationToken` and sets it in the `SecurityContextHolder`.
7.  The request proceeds to the Controller, now carrying the authenticated user's identity.

## 3. Core Use Case Flows

### 3.1 Sign Up Flow (Registration)

1.  **Client Request**: The frontend sends a `POST /api/signup` request with a JSON body (name, email, password, role).
2.  **Controller (`AuthController`)**: Receives the request, automatically maps the JSON to a `SignUpRequest` DTO via `@RequestBody`, and passes it to `AuthService`.
3.  **Service (`AuthService.registerUser`)**:
    - Checks `UserRepository` if the email already exists; throws an exception if it does.
    - Creates a new `User` entity.
    - Hashes the password using `BCryptPasswordEncoder`.
    - Sets the role (defaults to `USER` if empty) and capitalizes it.
    - Calls `UserRepository.save(user)` to persist the user in the database.
4.  **Response**: The newly created `User` entity is returned to the frontend with a `201 Created` status.

### 3.2 Sign In Flow (Authentication)

1.  **Client Request**: The frontend sends a `POST /api/signin` with email and password (`LoginRequest`).
2.  **Controller (`AuthController`)**: Passes the request to `AuthService.authenticateUser`.
3.  **Service Validation (`AuthService`)**:
    - Creates an unauthenticated `UsernamePasswordAuthenticationToken` with the provided credentials.
    - Passes it to `AuthenticationManager.authenticate(...)`.
    - _Behind the scenes_: `DaoAuthenticationProvider` uses `CustomUserDetailsService` to fetch the user from the database by email, then uses `PasswordEncoder` to check if the provided password matches the hashed password.
4.  **Token Generation**: Upon successful authentication, `JwtTokenProvider.generateToken` is called. It crafts a JWT containing the email, role, issue time, and expiration time, signed with the server's secret key.
5.  **Response**: The token is packaged into a `JwtAuthenticationResponse` and returned to the client with a `200 OK` status.

### 3.3 Accessing Protected Data (e.g., Get All Users)

1.  **Client Request**: Frontend sends a `GET /api/users` request, including the JWT in the `Authorization: Bearer <token>` header.
2.  **Security Filter**: `JwtAuthenticationFilter` intercepts, validates the token, extracts the user details, and authenticates the request context.
3.  **Authorization Check**: Spring Security checks if the path `/api/users` requires a specific role (e.g., `ADMIN`). If the authenticated user has the necessary role, the request proceeds.
4.  **Controller & Service**: `AuthController.getAllUsers()` is invoked, calling `AuthService.findAllUsers()`, which queries the database via `UserRepository`.
5.  **Response**: A list of `User` entities is returned as JSON.

## 4. Bootstrapping (Data Initializer)

When the Spring application starts, the `DataInitializer` bean implements a `CommandLineRunner`.

- It checks if an admin user (`admin@example.com`) exists in the database.
- If it does not exist, it automatically creates and saves a default `ADMIN` user. This ensures the application always has a master account for administrative purposes immediately after deployment.
