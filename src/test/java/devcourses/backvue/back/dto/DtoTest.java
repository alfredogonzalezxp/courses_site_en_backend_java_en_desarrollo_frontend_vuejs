package devcourses.backvue.back.dto;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for DTO classes: LoginRequest, SignUpRequest, JwtAuthenticationResponse.
 * Tests getters and setters.
 */
@DisplayName("DTO Tests")
class DtoTest {

    // =========================================================================
    // LoginRequest Tests
    // =========================================================================
    @Test
    @DisplayName("LoginRequest - should set and get email")
    void loginRequestShouldSetAndGetEmail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        assertThat(request.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("LoginRequest - should set and get password")
    void loginRequestShouldSetAndGetPassword() {
        LoginRequest request = new LoginRequest();
        request.setPassword("pass123");
        assertThat(request.getPassword()).isEqualTo("pass123");
    }

    @Test
    @DisplayName("LoginRequest - no-arg constructor should create empty object")
    void loginRequestNoArgConstructor() {
        LoginRequest request = new LoginRequest();
        assertThat(request.getEmail()).isNull();
        assertThat(request.getPassword()).isNull();
    }

    // =========================================================================
    // SignUpRequest Tests
    // =========================================================================
    @Test
    @DisplayName("SignUpRequest - should set and get nombre")
    void signUpRequestShouldSetAndGetNombre() {
        SignUpRequest request = new SignUpRequest();
        request.setNombre("John");
        assertThat(request.getNombre()).isEqualTo("John");
    }

    @Test
    @DisplayName("SignUpRequest - should set and get email")
    void signUpRequestShouldSetAndGetEmail() {
        SignUpRequest request = new SignUpRequest();
        request.setEmail("john@example.com");
        assertThat(request.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("SignUpRequest - should set and get password")
    void signUpRequestShouldSetAndGetPassword() {
        SignUpRequest request = new SignUpRequest();
        request.setPassword("secure123");
        assertThat(request.getPassword()).isEqualTo("secure123");
    }

    @Test
    @DisplayName("SignUpRequest - should get rol")
    void signUpRequestShouldGetRol() {
        SignUpRequest request = new SignUpRequest();
        assertThat(request.getRol()).isNull();
    }
}
