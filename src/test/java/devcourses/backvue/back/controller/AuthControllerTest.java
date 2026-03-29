package devcourses.backvue.back.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import devcourses.backvue.back.dto.LoginRequest;
import devcourses.backvue.back.dto.SignUpRequest;
import devcourses.backvue.back.model.User;
import devcourses.backvue.back.security.CustomUserDetailsService;
import devcourses.backvue.back.security.JwtAuthenticationFilter;
import devcourses.backvue.back.security.JwtTokenProvider;
import devcourses.backvue.back.security.SecurityConfig;
import devcourses.backvue.back.service.AuthService;

/**
 * Integration tests for AuthController using @WebMvcTest.
 * Tests HTTP endpoints, status codes, and JSON responses.
 * Uses MockitoBean to replace real service with mocks.
 */
@WebMvcTest(AuthController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private LoginRequest loginRequest;
    private SignUpRequest signUpRequest;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");

        signUpRequest = new SignUpRequest();
        signUpRequest.setNombre("John Doe");
        signUpRequest.setEmail("john@example.com");
        signUpRequest.setPassword("password123");

        sampleUser = new User("john@example.com", "John Doe", "encodedPass", "USER");
    }

    // =========================================================================
    // POST /api/signin Tests
    // =========================================================================
    @Nested
    @DisplayName("POST /api/signin")
    class SignIn {

        @Test
        @DisplayName("Should return 200 and JWT token on successful login")
        void shouldReturn200WithTokenOnSuccess() throws Exception {
            when(authService.authenticateUser(any(LoginRequest.class)))
                    .thenReturn("mock-jwt-token-123");

            mockMvc.perform(post("/api/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("mock-jwt-token-123"));
        }

        @Test
        @DisplayName("Should return 401 on invalid credentials")
        void shouldReturn401OnInvalidCredentials() throws Exception {
            when(authService.authenticateUser(any(LoginRequest.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            mockMvc.perform(post("/api/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // POST /api/signup Tests
    // =========================================================================
    @Nested
    @DisplayName("POST /api/signup")
    class SignUp {

        @Test
        @DisplayName("Should return 201 when user registered successfully")
        void shouldReturn201OnSuccessfulRegistration() throws Exception {
            when(authService.registerUser(any(SignUpRequest.class)))
                    .thenReturn(sampleUser);

            mockMvc.perform(post("/api/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("john@example.com"))
                    .andExpect(jsonPath("$.nombre").value("John Doe"));
        }
    }

    // =========================================================================
    // GET /api/health Tests
    // =========================================================================
    @Nested
    @DisplayName("GET /api/health")
    class HealthCheck {

        @Test
        @DisplayName("Should return 200 OK for health check (public endpoint)")
        void shouldReturn200ForHealthCheck() throws Exception {
            mockMvc.perform(get("/api/health"))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // GET /api/users Tests (requires ADMIN)
    // =========================================================================
    @Nested
    @DisplayName("GET /api/users")
    class GetAllUsers {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 and user list for ADMIN")
        void shouldReturn200ForAdmin() throws Exception {
            when(authService.findAllUsers()).thenReturn(List.of(sampleUser));

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].email").value("john@example.com"));
        }

        @Test
        @DisplayName("Should return 401 for unauthenticated user")
        void shouldReturn401ForUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should return 403 for non-ADMIN user")
        void shouldReturn403ForNonAdmin() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // DELETE /api/users/{id} Tests
    // =========================================================================
    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUser {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 when admin deletes user")
        void shouldReturn200WhenAdminDeletes() throws Exception {
            doNothing().when(authService).deleteUser(1L);

            mockMvc.perform(delete("/api/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("User deleted successfully"));
        }

        @Test
        @DisplayName("Should return 401 when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(delete("/api/users/1"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // PUT /api/users/{id} Tests
    // =========================================================================
    @Nested
    @DisplayName("PUT /api/users/{id}")
    class UpdateUser {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 200 with updated user for ADMIN")
        void shouldReturn200WithUpdatedUser() throws Exception {
            User updatedUser = new User("updated@example.com", "Updated Name", "encoded", "USER");
            when(authService.updateUser(eq(1L), any(SignUpRequest.class)))
                    .thenReturn(updatedUser);

            mockMvc.perform(put("/api/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Updated Name"))
                    .andExpect(jsonPath("$.email").value("updated@example.com"));
        }
    }
}
