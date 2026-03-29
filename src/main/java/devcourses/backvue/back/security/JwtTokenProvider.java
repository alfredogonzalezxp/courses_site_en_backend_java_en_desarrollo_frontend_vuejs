package devcourses.backvue.back.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * JwtTokenProvider is a utility class responsible for all operations related to
 * JWTs
 * (JSON Web Tokens). It handles the creation of new tokens when users log in,
 * extracts information (like the username/email) from existing tokens, and
 * verifies the cryptographic validity of tokens received in API requests.
 */
@Component
public class JwtTokenProvider {

    // The cryptographic algorithm used to sign and verify tokens
    private final Algorithm algorithm;

    // The time in milliseconds that a token remains valid after being issued
    private final long jwtExpirationMs;

    /**
     * Constructor for JwtTokenProvider.
     * It reads the secret and expiration time from the application's configuration
     * (e.g., application.properties or application.yml) and initializes the
     * HMAC256 algorithm used for token signing.
     *
     * @param secret          The secret key used to sign the tokens (must be kept
     *                        secure).
     * @param jwtExpirationMs The token's lifespan in milliseconds.
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long jwtExpirationMs) {
        // Initialize the HMAC256 signing algorithm with the provided secret key
        this.algorithm = Algorithm.HMAC256(secret);
        // Store the expiration time for later use when generating new tokens
        this.jwtExpirationMs = jwtExpirationMs;
    }

    /**
     * Generates a new JWT for an authenticated user.
     * This method is typically called right after a user successfully logs in
     * with their credentials.
     *
     * @param authentication The Spring Security Authentication object containing
     *                       the user's details.
     * @return A freshly minted, cryptographically signed JWT string.
     */
    public String generateToken(Authentication authentication) {
        // 1. Get the primary identifier (usually the email or username) from the auth
        // object
        String username = authentication.getName(); // aquí será email

        // 2. Determine the current time (when the token is issued)
        Date now = new Date();

        // 3. Calculate the exact moment when the token will expire
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // 4. Extract the user's primary role to include it in the token
        // This takes the first authority (e.g., "ROLE_ADMIN"), removes the "ROLE_"
        // prefix,
        // and defaults to "USER" if no roles are found.
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .orElse("USER");

        // 5. Build and return the JWT using the Auth0 library
        return JWT.create()
                .withSubject(username) // The main identifier (email/username)
                .withClaim("role", role) // Custom claim: the user's role
                .withIssuedAt(now) // Timestamp of creation
                .withExpiresAt(expiryDate) // Timestamp of expiration
                .sign(algorithm); // Cryptographically sign the whole package
    }

    /**
     * Extracts the username (or email) embedded within the JWT.
     * This is used by the authentication filter to know who the requester is.
     *
     * @param token The JWT string.
     * @return The subject (username/email) extracted from the token's payload.
     */
    public String getUsernameFromToken(String token) {
        // 1. Require the expected generic algorithm structure and verify the token's
        // signature
        // 2. Decode the token to access its payload
        DecodedJWT decoded = JWT.require(algorithm).build().verify(token);

        // 3. Return the "Subject" claim which we previously set as the username/email
        return decoded.getSubject();
    }

    /**
     * Validates if a given JWT is structurally correct, hasn't expired,
     * and has a valid cryptographic signature.
     *
     * @param token The JWT string to validate.
     * @return true if the token is completely valid; false if it's expired,
     *         tampered with, or otherwise invalid.
     */
    public boolean validateToken(String token) {
        try {
            // Attempt to decode and verify the token signature and expiration
            JWT.require(algorithm).build().verify(token);
            // If the verify() method finishes without throwing an exception, the token is
            // perfectly valid
            return true;
        } catch (Exception ex) {
            // If ANY exception occurs (e.g., TokenExpiredException,
            // SignatureVerificationException),
            // catch it and return false, meaning the token should be rejected.
            return false;
        }
    }
}