package devcourses.backvue.back.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import devcourses.backvue.back.model.User;
import devcourses.backvue.back.repository.UserRepository;

/**
 * Unit tests for CustomUserDetailsService.
 * Tests the loadUserByUsername method which is called by Spring Security
 * during the authentication process.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("john@example.com", "John Doe", "encodedPassword", "ADMIN");
    }

    @Test
    @DisplayName("Should return UserDetails when user exists by email")
    void shouldReturnUserDetailsWhenUserExists() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(sampleUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john@example.com");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("john@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    @DisplayName("Should return UserDetails with correct authorities")
    void shouldReturnCorrectAuthorities() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(sampleUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john@example.com");

        // Assert - user has ADMIN role, so authority should be ROLE_ADMIN
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found")
    void shouldThrowWhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email: unknown@example.com");
    }

    @Test
    @DisplayName("Should return CustomUserDetails instance")
    void shouldReturnCustomUserDetailsInstance() {
        // Arrange
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(sampleUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("john@example.com");

        // Assert
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);
    }
}
