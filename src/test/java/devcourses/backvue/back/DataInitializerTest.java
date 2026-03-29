package devcourses.backvue.back;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import devcourses.backvue.back.model.User;
import devcourses.backvue.back.repository.UserRepository;

/**
 * Unit tests for DataInitializer.
 * Tests the logic that creates the default admin user on startup.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DataInitializer Tests")
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    @DisplayName("Should create admin user when admin does not exist")
    void shouldCreateAdminWhenNotExists() throws Exception {
        // Arrange
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("alf")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        dataInitializer.run();

        // Assert
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should NOT create admin user when admin already exists")
    void shouldNotCreateAdminWhenAlreadyExists() throws Exception {
        // Arrange
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);

        // Act
        dataInitializer.run();

        // Assert - save should never be called
        verify(userRepository, never()).save(any(User.class));
    }
}
