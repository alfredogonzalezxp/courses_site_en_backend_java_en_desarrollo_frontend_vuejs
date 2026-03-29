package devcourses.backvue.back.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import devcourses.backvue.back.model.User;

/**
 * Integration tests for UserRepository.
 * Uses @DataJpaTest which loads only the JPA components and uses
 * H2 in-memory database (from application-test.properties).
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // =========================================================================
    // findByEmail() Tests
    // =========================================================================
    @Nested
    @DisplayName("findByEmail()")
    class FindByEmail {

        @Test
        @DisplayName("Should find user by email when user exists")
        void shouldFindUserByEmail() {
            // Arrange
            User user = new User("john@example.com", "John", "password", "USER");
            userRepository.save(user);

            // Act
            Optional<User> found = userRepository.findByEmail("john@example.com");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getNombre()).isEqualTo("John");
            assertThat(found.get().getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("Should return empty when email not found")
        void shouldReturnEmptyWhenEmailNotFound() {
            // Act
            Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

            // Assert
            assertThat(found).isEmpty();
        }
    }

    // =========================================================================
    // existsByEmail() Tests
    // =========================================================================
    @Nested
    @DisplayName("existsByEmail()")
    class ExistsByEmail {

        @Test
        @DisplayName("Should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            // Arrange
            User user = new User("john@example.com", "John", "password", "USER");
            userRepository.save(user);

            // Act & Assert
            assertThat(userRepository.existsByEmail("john@example.com")).isTrue();
        }

        @Test
        @DisplayName("Should return false when email does not exist")
        void shouldReturnFalseWhenEmailNotExists() {
            assertThat(userRepository.existsByEmail("ghost@example.com")).isFalse();
        }
    }

    // =========================================================================
    // CRUD Operations (Inherited from JpaRepository)
    // =========================================================================
    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should save and retrieve a user")
        void shouldSaveAndRetrieveUser() {
            // Arrange
            User user = new User("save@example.com", "SaveTest", "pass", "USER");

            // Act
            User saved = userRepository.save(user);

            // Assert
            assertThat(saved.getId()).isNotNull();
            assertThat(userRepository.findById(saved.getId())).isPresent();
        }

        @Test
        @DisplayName("Should find all users")
        void shouldFindAllUsers() {
            // Arrange
            userRepository.save(new User("a@example.com", "A", "pass", "USER"));
            userRepository.save(new User("b@example.com", "B", "pass", "ADMIN"));

            // Act & Assert
            assertThat(userRepository.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("Should delete user by ID")
        void shouldDeleteUserById() {
            // Arrange
            User user = userRepository.save(new User("delete@example.com", "Delete", "pass", "USER"));

            // Act
            userRepository.deleteById(user.getId());

            // Assert
            assertThat(userRepository.findById(user.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should update existing user")
        void shouldUpdateExistingUser() {
            // Arrange
            User user = userRepository.save(new User("update@example.com", "Original", "pass", "USER"));

            // Act
            user.setNombre("Updated Name");
            User updated = userRepository.save(user);

            // Assert
            assertThat(updated.getNombre()).isEqualTo("Updated Name");
            assertThat(userRepository.count()).isEqualTo(1); // No duplicate created
        }

        @Test
        @DisplayName("Should count users correctly")
        void shouldCountUsers() {
            userRepository.save(new User("a@example.com", "A", "p", "USER"));
            userRepository.save(new User("b@example.com", "B", "p", "USER"));
            userRepository.save(new User("c@example.com", "C", "p", "ADMIN"));

            assertThat(userRepository.count()).isEqualTo(3);
        }
    }
}
