package devcourses.backvue.back.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * @Configuration indicates that this class declares @Bean methods and may be
 *                processed
 *                by the Spring container to generate bean definitions and
 *                service requests at runtime.
 */
@Configuration
/**
 * @EnableWebSecurity applies Spring Security to the application's web security
 *                    configuration.
 *                    It allows Spring to find and automatically apply the class
 *                    to the global WebSecurity.
 */
@EnableWebSecurity
public class SecurityConfig {

    // userDetailsService is responsible for retrieving the user's details (like
    // username, password, roles) from the database
    private final CustomUserDetailsService userDetailsService;

    // jwtAuthenticationFilter is a custom filter that intercepts requests to
    // validate JWT tokens
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Constructor injection for the dependencies. Spring automatically provides
     * instances of CustomUserDetailsService and JwtAuthenticationFilter when it
     * creates SecurityConfig.
     */
    public SecurityConfig(CustomUserDetailsService userDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * @Bean tells Spring to manage the returned object.
     *       PasswordEncoder is used to securely hash passwords before storing them
     *       in the database,
     *       and to verify passwords when users attempt to log in. In this case, we
     *       use BCrypt.
     *       Bean: "Run this method once, take the object it creates,
     *       and make that object available to be used anywhere in the
     *       project."
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * DaoAuthenticationProvider is an AuthenticationProvider that retrieves user
     * details
     * from a UserDetailsService. It uses the PasswordEncoder to compare the
     * submitted
     * password against the hashed password stored in the database.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        // Tells the provider how to fetch user data (from our database).
        // IMPORTANT FLOW:
        // 1. The provider does NOT keep user data in memory permanently.
        // 2. ONLY at the exact millisecond a user tries to log in (e.g. POST
        // /api/signin),
        // the provider calls this 'userDetailsService'.
        // 3. 'userDetailsService' runs a query in the database to get the hashed
        // password.
        // 4. The provider compares the typed password against the database password.
        // 5. It immediately discards the data after verifying.
        provider.setUserDetailsService(userDetailsService);

        // Once the provider has the user's data from the database, it can then take the
        // plain text password the user typed, hash it, and see if
        // it perfectly matches the hashed password from the database.
        provider.setUserDetailsService(userDetailsService);

        // Tells the provider which hashing algorithm to use to verify passwords
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * The AuthenticationManager is the main interface for processing authentication
     * requests.
     * Here we configure it to use our DaoAuthenticationProvider via a
     * ProviderManager.
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        // ProviderManager delegates the authentication attempt to the
        // DaoAuthenticationProvider we defined above
        return new ProviderManager(authenticationProvider());
    }

    /**
     * The SecurityFilterChain defines the security rules for HTTP requests.
     * This is the core of our web security configuration.
     * 
     * @param http the HttpSecurity builder used to configure web security rules.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {

        http
                // Disable CSRF (Cross-Site Request Forgery) protection:
                // Since we are using stateless JWT tokens instead of session cookies, CSRF is
                // not applicable.
                .csrf(csrf -> csrf.disable())

                // Enable CORS (Cross-Origin Resource Sharing):
                // This tells Spring Security to use the corsConfigurationSource() bean defined
                // below
                // to decide which cross-origin requests are allowed (e.g., from our Vue.js
                // frontend).
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless Session Management:
                // We tell Spring not to create HTTP sessions. Every request must bring its own
                // JWT token.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure authorization rules for specific endpoints:
                .authorizeHttpRequests(auth -> auth
                        // Permit all requests (no authentication required) to the login, register, and
                        // health endpoints
                        .requestMatchers("/api/signin", "/api/signup", "/api/health").permitAll()

                        // Restrict access to any endpoint starting with "/api/users/" to users who have
                        // the "ADMIN" role
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // Allow all preflight (OPTIONS) requests. Browsers send OPTIONS requests first
                        // to check CORS permissions.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Any other request not specified above must be authenticated (user must be
                        // logged in and provide a valid token)
                        .anyRequest().authenticated())

                // Exception Handling for authentication errors:
                .exceptionHandling(exception -> exception
                        // If an unauthenticated user tries to access a protected resource, return a 401
                        // Unauthorized status
                        // instead of trying to redirect them to a default Spring login HTML page.
                        .authenticationEntryPoint(
                                new org.springframework.security.web.authentication.HttpStatusEntryPoint(
                                        org.springframework.http.HttpStatus.UNAUTHORIZED)));

        // Add our custom JWT filter BEFORE the standard Spring Security
        // username/password filter.
        // This ensures every request is checked for a JWT token before standard
        // authentication processing occurs.
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Build and return the complete filter chain configuration
        return http.build();
    }

    /**
     * Defines the CORS configuration rules for the application.
     * This determines which external domains (frontends) are allowed to communicate
     * with our API.
     */
    /*
     * @Bean tells Spring: "Run this code once, keep the object it
     * makes, and share it with anyone in the project who asks for
     * it."
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allowed Origins:
        // Specify exactly which frontend applications can make requests to this
        // backend.
        // E.g., React/Vue dev servers running on localhost.
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));

        // Allowed Methods:
        // Specify which HTTP methods are permitted for cross-origin requests.
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allowed Headers:
        // Permit any HTTP headers to be sent in the request (e.g., Authorization,
        // Content-Type).
        config.setAllowedHeaders(List.of("*"));

        // Allow Credentials:
        // By setting this to true, the browser is permitted to include credentials like
        // cookies
        // or authorization headers (like our JWT Bearer token) in cross-origin
        // requests.
        config.setAllowCredentials(true);

        // Register this configuration for ALL routes in the API ("/**")
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}