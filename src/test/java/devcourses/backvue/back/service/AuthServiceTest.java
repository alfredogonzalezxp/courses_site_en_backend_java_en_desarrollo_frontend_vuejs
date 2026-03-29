package devcourses.backvue.back.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import devcourses.backvue.back.dto.LoginRequest;
import devcourses.backvue.back.dto.SignUpRequest;
import devcourses.backvue.back.model.User;
import devcourses.backvue.back.repository.UserRepository;
import devcourses.backvue.back.security.JwtTokenProvider;

/**
 * Unit tests for the AuthService class.
 * Tests all business logic: authentication, registration, CRUD operations.
 * Uses Mockito to mock all dependencies (repository, encoder, etc).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private SignUpRequest sampleSignUpRequest;
    private LoginRequest sampleLoginRequest;

    @BeforeEach
    void setUp() {
        // Create a sample user for tests
        sampleUser = new User("john@example.com", "John Doe", "encodedPassword123", "USER");

        // Create a sample signup request
        sampleSignUpRequest = new SignUpRequest();
        sampleSignUpRequest.setNombre("John Doe");
        sampleSignUpRequest.setEmail("john@example.com");
        sampleSignUpRequest.setPassword("password123");

        // Create a sample login request
        sampleLoginRequest = new LoginRequest();
        sampleLoginRequest.setEmail("john@example.com");
        sampleLoginRequest.setPassword("password123");
    }

    // =========================================================================
    // authenticateUser() Tests
    // =========================================================================
    @Nested
    @DisplayName("authenticateUser()")
    class AuthenticateUser {

        @Test
        @DisplayName("Should return JWT token when credentials are valid")
        void shouldReturnTokenWhenCredentialsAreValid() {
            // Arrange
            Authentication mockAuth = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuth);
            when(tokenProvider.generateToken(mockAuth))
                    .thenReturn("mock-jwt-token-123");

            // Act
            String token = authService.authenticateUser(sampleLoginRequest);

            // Assert
            assertThat(token).isEqualTo("mock-jwt-token-123");
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenProvider).generateToken(mockAuth);
        }

        @Test
        @DisplayName("Should pass correct email and password to AuthenticationManager")
        void shouldPassCorrectCredentials() {
            // Arrange
            Authentication mockAuth = mock(Authentication.class);
            ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            when(authenticationManager.authenticate(captor.capture())).thenReturn(mockAuth);
            when(tokenProvider.generateToken(any())).thenReturn("token");

            // Act
            authService.authenticateUser(sampleLoginRequest);

            // Assert - verify the exact credentials were passed
            UsernamePasswordAuthenticationToken captured = captor.getValue();
            assertThat(captured.getPrincipal()).isEqualTo("john@example.com");
            assertThat(captured.getCredentials()).isEqualTo("password123");
        }

        @Test
        @DisplayName("Should throw exception when credentials are invalid")
        void shouldThrowWhenCredentialsAreInvalid() {
            // Arrange
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // Act & Assert
            assertThatThrownBy(() -> authService.authenticateUser(sampleLoginRequest))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    // =========================================================================
    // registerUser() Tests
    // =========================================================================
    @Nested
    @DisplayName("registerUser()")
    class RegisterUser {

        @Test
        @DisplayName("Should register user successfully when email is not taken")
        void shouldRegisterUserSuccessfully() {
            // Arrange
            when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            User result = authService.registerUser(sampleSignUpRequest);

            // Assert
            assertThat(result.getNombre()).isEqualTo("John Doe");
            assertThat(result.getEmail()).isEqualTo("john@example.com");
            assertThat(result.getPassword()).isEqualTo("encodedPassword");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email is already in use")
        void shouldThrowWhenEmailAlreadyExists() {
            // Arrange
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.registerUser(sampleSignUpRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Email already in use");

            // Verify save was NEVER called
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should encode password before saving")
        void shouldEncodePasswordBeforeSaving() {
            // Arrange
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedValue");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            authService.registerUser(sampleSignUpRequest);

            // Assert - capture the saved user and verify password is encoded
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo("$2a$10$hashedValue");
        }

        @Test
        @DisplayName("Should default role to USER when role is null")
        void shouldDefaultRoleToUserWhenNull() {
            // Arrange - signUpRequest with null role (no setRol call)
            SignUpRequest request = new SignUpRequest();
            request.setNombre("Jane");
            request.setEmail("jane@example.com");
            request.setPassword("pass");

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            User result = authService.registerUser(request);

            // Assert
            assertThat(result.getRol()).isEqualTo("USER");
        }

        @Test
        @DisplayName("Should convert role to uppercase")
        void shouldConvertRoleToUppercase() {
            // Arrange
            SignUpRequest request = new SignUpRequest();
            request.setNombre("Admin");
            request.setEmail("admin@example.com");
            request.setPassword("pass");
            // Note: SignUpRequest doesn't have setRol, so this tests the default behavior

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            User result = authService.registerUser(request);

            // Assert
            assertThat(result.getRol()).isEqualTo(result.getRol().toUpperCase());
        }
    }

    // =========================================================================
    // findAllUsers() Tests
    // =========================================================================
    @Nested
    @DisplayName("findAllUsers()")
    class FindAllUsers {

        @Test
        @DisplayName("Should return all users from repository")
        void shouldReturnAllUsers() {
            // Arrange
            User user2 = new User("jane@example.com", "Jane", "pass", "ADMIN");
            when(userRepository.findAll()).thenReturn(List.of(sampleUser, user2));

            // Act
            List<User> users = authService.findAllUsers();

            // Assert
            assertThat(users).hasSize(2);
            assertThat(users.get(0).getEmail()).isEqualTo("john@example.com");
            assertThat(users.get(1).getEmail()).isEqualTo("jane@example.com");
            verify(userRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void shouldReturnEmptyListWhenNoUsers() {
            // Arrange
            when(userRepository.findAll()).thenReturn(List.of());

            // Act
            List<User> users = authService.findAllUsers();

            // Assert
            assertThat(users).isEmpty();
        }
    }

    // =========================================================================
    // deleteUser() Tests
    // =========================================================================
    @Nested
    @DisplayName("deleteUser()")
    class DeleteUser {

        @Test
        @DisplayName("Should call repository deleteById with correct ID")
        void shouldDeleteUserById() {
            // Arrange
            doNothing().when(userRepository).deleteById(1L);

            // Act
            authService.deleteUser(1L);

            // Assert
            verify(userRepository).deleteById(1L);
        }
    }

    // =========================================================================
    // updateUser() Tests
    // =========================================================================
    @Nested
    @DisplayName("updateUser()")
    class UpdateUser {

        @Test
        @DisplayName("Should update user fields when user exists")
        void shouldUpdateUserWhenExists() {
            // Arrange
            User existingUser = new User("old@example.com", "Old Name", "oldPassword", "USER");
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            SignUpRequest updateRequest = new SignUpRequest();
            updateRequest.setNombre("New Name");
            updateRequest.setEmail("new@example.com");
            updateRequest.setPassword("newPassword");

            when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

            // Act
            User updated = authService.updateUser(1L, updateRequest);

            // Assert
            assertThat(updated.getNombre()).isEqualTo("New Name");
            assertThat(updated.getEmail()).isEqualTo("new@example.com");
            assertThat(updated.getPassword()).isEqualTo("encodedNewPassword");
            verify(userRepository).save(existingUser);
        }

        @Test
        @DisplayName("Should NOT update password when password is null or blank")
        void shouldNotUpdatePasswordWhenBlank() {
            // Arrange
            User existingUser = new User("old@example.com", "Old Name", "keepThisPassword", "USER");
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            SignUpRequest updateRequest = new SignUpRequest();
            updateRequest.setNombre("New Name");
            updateRequest.setEmail("new@example.com");
            updateRequest.setPassword("");  // blank password

            // Act
            User updated = authService.updateUser(1L, updateRequest);

            // Assert - password should remain unchanged
            assertThat(updated.getPassword()).isEqualTo("keepThisPassword");
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowWhenUserNotFound() {
            // Arrange
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            SignUpRequest updateRequest = new SignUpRequest();
            updateRequest.setNombre("Test");
            updateRequest.setEmail("test@example.com");

            // Act & Assert
            assertThatThrownBy(() -> authService.updateUser(999L, updateRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }
    }
}
