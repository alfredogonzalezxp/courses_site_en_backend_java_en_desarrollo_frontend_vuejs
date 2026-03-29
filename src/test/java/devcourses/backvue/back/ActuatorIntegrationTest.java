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
        @DisplayName("GET /actuator should be accessible without authentication")
        void baseActuatorShouldBePublic() throws Exception {
            mockMvc.perform(get("/actuator"))
                    .andExpect(status().isOk());
        }

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
