package devcourses.backvue.back.security;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import devcourses.backvue.back.model.User;

/**
 * Unit tests for CustomUserDetails.
 * Tests the adapter between our User entity and Spring Security's UserDetails interface.
 */
@DisplayName("CustomUserDetails Tests")
class CustomUserDetailsTest {

    private User user;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        user = new User("john@example.com", "John Doe", "encodedPassword", "ADMIN");
        customUserDetails = new CustomUserDetails(user);
    }

    @Test
    @DisplayName("Should return email as username")
    void shouldReturnEmailAsUsername() {
        assertThat(customUserDetails.getUsername()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("Should return encoded password")
    void shouldReturnPassword() {
        assertThat(customUserDetails.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("Should return nombre")
    void shouldReturnNombre() {
        assertThat(customUserDetails.getNombre()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return correct authority with ROLE_ prefix")
    void shouldReturnCorrectAuthority() {
        assertThat(customUserDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should return ROLE_USER for user role")
    void shouldReturnRoleUserAuthority() {
        User regularUser = new User("user@example.com", "Regular", "pass", "USER");
        CustomUserDetails details = new CustomUserDetails(regularUser);

        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("Account should be non-expired")
    void shouldBeNonExpired() {
        assertThat(customUserDetails.isAccountNonExpired()).isTrue();
    }

    @Test
    @DisplayName("Account should be non-locked")
    void shouldBeNonLocked() {
        assertThat(customUserDetails.isAccountNonLocked()).isTrue();
    }

    @Test
    @DisplayName("Credentials should be non-expired")
    void shouldHaveNonExpiredCredentials() {
        assertThat(customUserDetails.isCredentialsNonExpired()).isTrue();
    }

    @Test
    @DisplayName("Account should be enabled")
    void shouldBeEnabled() {
        assertThat(customUserDetails.isEnabled()).isTrue();
    }
}
