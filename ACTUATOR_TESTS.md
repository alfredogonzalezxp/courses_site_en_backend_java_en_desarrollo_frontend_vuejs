# Spring Boot Actuator Tests

This document contains the integration tests implemented to verify the **Spring Boot Actuator** configuration, specifically focusing on security rules and endpoint accessibility.

## Endpoints Under Test

The following Actuator endpoints are configured and verified in our test suite:

- **Public Endpoints (No Auth Required):**
  - `GET /actuator/health`: Provides comprehensive health status (status: UP/DOWN).
  - `GET /actuator/info`: Displays generic application details.

- **Protected Endpoints (Admin Required):**
  - `GET /actuator/metrics`: Exposes detailed runtime metrics (JVM, memory, threads, requests).
  - `GET /actuator`: Base page listing all available endpoints.

## Test Class: `ActuatorIntegrationTest.java`

Located at: `src/test/java/devcourses/backvue/back/ActuatorIntegrationTest.java`

```java
package devcourses.backvue.back;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for Spring Boot Actuator endpoints.
 * Verifies that security rules are correctly applied to actuator routes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Actuator Integration Tests")
class ActuatorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // =========================================================================
    // Public Endpoints (/actuator/health, /actuator/info)
    // =========================================================================
    @Nested
    @DisplayName("Public Actuator Endpoints")
    class PublicEndpoints {

        @Test
        @DisplayName("GET /actuator/health should be accessible without authentication")
        void healthShouldBePublic() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"));
        }

        @Test
        @DisplayName("GET /actuator/info should be accessible without authentication")
        void infoShouldBePublic() throws Exception {
            mockMvc.perform(get("/actuator/info"))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // Protected Endpoints (/actuator/metrics, etc.)
    // =========================================================================
    @Nested
    @DisplayName("Protected Actuator Endpoints")
    class ProtectedEndpoints {

        @Test
        @DisplayName("GET /actuator/metrics should be rejected for unauthenticated user")
        void metricsShouldBeRejectedForUnauthenticated() throws Exception {
            mockMvc.perform(get("/actuator/metrics"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("GET /actuator/metrics should be forbidden for regular USER")
        void metricsShouldBeForbiddenForUser() throws Exception {
            mockMvc.perform(get("/actuator/metrics"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("GET /actuator/metrics should be accessible for ADMIN")
        void metricsShouldBeAccessibleForAdmin() throws Exception {
            mockMvc.perform(get("/actuator/metrics"))
                    .andExpect(status().isOk());
        }
    }
}
```

## How to Run the Tests

You can execute these tests using the Gradle wrapper from the `back` directory:

```powershell
.\gradlew.bat test --tests "devcourses.backvue.back.ActuatorIntegrationTest"
```

## Test Coverage Summary

1.  **`/actuator/health`**: Publicly accessible. Verifies that the internal health of the application (DB connection, disk space, etc.) is positive.
2.  **`/actuator/info`**: Publicly accessible. Used for basic application information.
3.  **`/actuator/metrics`**: Protected.
    - Rejects unauthenticated requests (**401**).
    - Rejects users without the `ADMIN` role (**403**).
    - Grants access to users with the `ADMIN` role (**200**).

> [!IMPORTANT]
> These tests use the `@SpringBootTest` annotation, which loads the full application context. This ensures that the real `SecurityConfig` rules are being applied exactly as they would be in production.
