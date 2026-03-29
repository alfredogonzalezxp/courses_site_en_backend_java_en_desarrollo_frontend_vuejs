package devcourses.backvue.back.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

//I use this file in SecurityConfig.java

/**
 * JwtAuthenticationFilter is a custom security filter that runs once per HTTP
 * request.
 * Its main responsibility is to intercept incoming requests, extract the JWT
 * token,
 * validate it, and set up the Spring Security context if the token is valid.
 * This allows the application to authenticate users securely on a stateless API
 * basis.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    // From JwtTokenProvider.java
    private final CustomUserDetailsService userDetailsService;

    /**
     * Constructor for JwtAuthenticationFilter.
     * Injects the necessary dependencies to handle token validation and user
     * details retrieval.
     *
     * @param tokenProvider      Utility class to generate, validate, and extract
     *                           information from JWTs.
     * @param userDetailsService Custom service to load user-specific data from the
     *                           database.
     */
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
            CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * This method contains the core logic of the filter, executed for every
     * incoming HTTP request.
     * It checks if a valid JWT token is present in the request. If so, it
     * authenticates the user
     * and sets their authentication details in the SecurityContext.
     *
     * @param request     The incoming HTTP request.
     * @param response    The outgoing HTTP response.
     * @param filterChain The chain of filters to pass the request/response to the
     *                    next entity.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException      If an input or output error occurs.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Extract the JWT string from the request headers
        String jwt = getJwtFromRequest(request);

        // 2. If the token is found and is valid according to our JwtTokenProvider
        if (jwt != null && tokenProvider.validateToken(jwt)) {
            // 3. Extract the username (or user identifier) from the payload of the JWT
            String username = tokenProvider.getUsernameFromToken(jwt);

            // 4. Load the user's full details (like roles and authorities) from the
            // database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 5. Create an authentication token object with the user details and
            // authorities
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());

            // 6. Attach additional request-specific details (like IP address, session id,
            // etc.)
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 7. Store the authentication object in the Spring Security context.
            // This officially authenticates the user for this specific request.
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 8. Continue the filter chain execution, allowing the request to proceed to
        // the next filter or target controller
        filterChain.doFilter(request, response);
    }

    /**
     * Helper method to extract the JWT token from the HTTP "Authorization" header.
     *
     * @param request The incoming HTTP request.
     * @return The JWT token string if present and validly formatted; otherwise,
     *         null.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // Retrieve the "Authorization" header from the HTTP request
        String bearerToken = request.getHeader("Authorization");

        // Check if the header exists and starts with the standard "Bearer " prefix
        // (formato: "Bearer <token>")
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // Extract and return just the token part, stripping away the first 7 characters
            // ("Bearer ")
            return bearerToken.substring(7);
        }

        // Return null if the token is not present or improperly formatted
        return null;
    }
}