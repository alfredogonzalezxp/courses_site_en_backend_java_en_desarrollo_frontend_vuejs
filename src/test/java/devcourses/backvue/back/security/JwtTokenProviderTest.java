package devcourses.backvue.back.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Unit tests for JwtTokenProvider.
 * Tests token generation, validation, and username extraction.
 */
@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    // Test secret and expiration
    private static final String TEST_SECRET = "TestSecretKeyForJUnitTestingPurposesOnly1234567890ABCDEF";
    private static final long TEST_EXPIRATION_MS = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(TEST_SECRET, TEST_EXPIRATION_MS);
    }

    // Helper method to create a mock Authentication
    private Authentication createMockAuthentication(String email, String role) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);

        @SuppressWarnings("unchecked")
        Collection<GrantedAuthority> authorities =
                (Collection<GrantedAuthority>) (Collection<?>) List.of(new SimpleGrantedAuthority("ROLE_" + role));
        doReturn(authorities).when(auth).getAuthorities();

        return auth;
    }

    // =========================================================================
    // generateToken() Tests
    // =========================================================================
    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("Should generate a non-null token")
        void shouldGenerateNonNullToken() {
            Authentication auth = createMockAuthentication("user@example.com", "USER");

            String token = tokenProvider.generateToken(auth);

            assertThat(token).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("Should generate token with 3 parts (header.payload.signature)")
        void shouldGenerateTokenWith3Parts() {
            Authentication auth = createMockAuthentication("user@example.com", "USER");

            String token = tokenProvider.generateToken(auth);

            // JWT tokens have 3 parts separated by dots
            String[] parts = token.split("\\.");
            assertThat(parts).hasSize(3);
        }

        @Test
        @DisplayName("Should generate different tokens for different users")
        void shouldGenerateDifferentTokensForDifferentUsers() {
            Authentication auth1 = createMockAuthentication("user1@example.com", "USER");
            Authentication auth2 = createMockAuthentication("user2@example.com", "ADMIN");

            String token1 = tokenProvider.generateToken(auth1);
            String token2 = tokenProvider.generateToken(auth2);

            assertThat(token1).isNotEqualTo(token2);
        }
    }

    // =========================================================================
    // getUsernameFromToken() Tests
    // =========================================================================
    @Nested
    @DisplayName("getUsernameFromToken()")
    class GetUsernameFromToken {

        @Test
        @DisplayName("Should extract correct email from token")
        void shouldExtractCorrectEmail() {
            Authentication auth = createMockAuthentication("john@example.com", "USER");
            String token = tokenProvider.generateToken(auth);

            String username = tokenProvider.getUsernameFromToken(token);

            assertThat(username).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("Should extract correct email for admin user")
        void shouldExtractCorrectEmailForAdmin() {
            Authentication auth = createMockAuthentication("admin@example.com", "ADMIN");
            String token = tokenProvider.generateToken(auth);

            String username = tokenProvider.getUsernameFromToken(token);

            assertThat(username).isEqualTo("admin@example.com");
        }
    }

    // =========================================================================
    // validateToken() Tests
    // =========================================================================
    @Nested
    @DisplayName("validateToken()")
    class ValidateToken {

        @Test
        @DisplayName("Should return true for a valid token")
        void shouldReturnTrueForValidToken() {
            Authentication auth = createMockAuthentication("user@example.com", "USER");
            String token = tokenProvider.generateToken(auth);

            boolean isValid = tokenProvider.validateToken(token);

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should return false for an invalid token")
        void shouldReturnFalseForInvalidToken() {
            boolean isValid = tokenProvider.validateToken("this.is.not.a.valid.token");

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should return false for a tampered token")
        void shouldReturnFalseForTamperedToken() {
            Authentication auth = createMockAuthentication("user@example.com", "USER");
            String token = tokenProvider.generateToken(auth);

            // Robust tampering: Modify the very first character of the token.
            // This is guaranteed to change the header/payload and invalidate the signature.
            char firstChar = token.charAt(0);
            char replacement = (firstChar == 'e') ? 'a' : 'e'; // JWTs usually start with 'e' (ey...)
            String tamperedToken = replacement + token.substring(1);

            boolean isValid = tokenProvider.validateToken(tamperedToken);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should return false for an expired token")
        void shouldReturnFalseForExpiredToken() {
            // Create a provider with 0ms expiration (token expires immediately)
            JwtTokenProvider expiredProvider = new JwtTokenProvider(TEST_SECRET, 0L);
            Authentication auth = createMockAuthentication("user@example.com", "USER");
            String token = expiredProvider.generateToken(auth);

            // Small delay to ensure expiration
            try { Thread.sleep(100); } catch (InterruptedException e) { /* ignore */ }

            boolean isValid = expiredProvider.validateToken(token);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty string token")
        void shouldReturnFalseForEmptyToken() {
            boolean isValid = tokenProvider.validateToken("");

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should return false for token signed with different secret")
        void shouldReturnFalseForTokenWithDifferentSecret() {
            // Generate token with a different secret
            JwtTokenProvider otherProvider = new JwtTokenProvider("DifferentSecretKey1234567890ABCDEFGHIJKLMNOP", TEST_EXPIRATION_MS);
            Authentication auth = createMockAuthentication("user@example.com", "USER");
            String token = otherProvider.generateToken(auth);

            // Validate with our provider (different secret)
            boolean isValid = tokenProvider.validateToken(token);

            assertThat(isValid).isFalse();
        }
    }

    // =========================================================================
    // Integration: Generate → Extract → Validate
    // =========================================================================
    @Nested
    @DisplayName("Full token lifecycle")
    class FullLifecycle {

        @Test
        @DisplayName("Should generate, extract, and validate a complete token")
        void shouldHandleFullTokenLifecycle() {
            // Generate
            Authentication auth = createMockAuthentication("lifecycle@example.com", "ADMIN");
            String token = tokenProvider.generateToken(auth);

            // Validate
            assertThat(tokenProvider.validateToken(token)).isTrue();

            // Extract
            assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo("lifecycle@example.com");
        }
    }
}
