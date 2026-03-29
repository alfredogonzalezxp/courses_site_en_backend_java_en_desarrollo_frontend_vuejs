package devcourses.backvue.back.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the User entity.
 * Tests constructors, getters, and setters.
 */
@DisplayName("User Entity Tests")
class UserTest {

    // =========================================================================
    // Constructor Tests
    // =========================================================================
    @Nested
    @DisplayName("Constructors")
    class Constructors {

        @Test
        @DisplayName("No-arg constructor should create empty user")
        void noArgConstructorShouldCreateEmptyUser() {
            User user = new User();

            assertThat(user.getId()).isNull();
            assertThat(user.getEmail()).isNull();
            assertThat(user.getNombre()).isNull();
            assertThat(user.getPassword()).isNull();
            assertThat(user.getRol()).isNull();
        }

        @Test
        @DisplayName("Parameterized constructor should set all fields")
        void parameterizedConstructorShouldSetFields() {
            User user = new User("john@example.com", "John Doe", "password123", "ADMIN");

            assertThat(user.getEmail()).isEqualTo("john@example.com");
            assertThat(user.getNombre()).isEqualTo("John Doe");
            assertThat(user.getPassword()).isEqualTo("password123");
            assertThat(user.getRol()).isEqualTo("ADMIN");
        }
    }

    // =========================================================================
    // Getter/Setter Tests
    // =========================================================================
    @Nested
    @DisplayName("Getters and Setters")
    class GettersAndSetters {

        private User user;

        @BeforeEach
        void setUp() {
            user = new User();
        }

        @Test
        @DisplayName("Should set and get email")
        void shouldSetAndGetEmail() {
            user.setEmail("test@example.com");
            assertThat(user.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should set and get nombre")
        void shouldSetAndGetNombre() {
            user.setNombre("Jane Doe");
            assertThat(user.getNombre()).isEqualTo("Jane Doe");
        }

        @Test
        @DisplayName("Should set and get password")
        void shouldSetAndGetPassword() {
            user.setPassword("securePassword");
            assertThat(user.getPassword()).isEqualTo("securePassword");
        }

        @Test
        @DisplayName("Should set and get rol")
        void shouldSetAndGetRol() {
            user.setRol("USER");
            assertThat(user.getRol()).isEqualTo("USER");
        }

        @Test
        @DisplayName("Id should be null before persisting (set by JPA)")
        void idShouldBeNullBeforePersisting() {
            assertThat(user.getId()).isNull();
        }
    }
}
