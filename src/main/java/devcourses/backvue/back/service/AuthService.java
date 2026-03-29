package devcourses.backvue.back.service;

import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import devcourses.backvue.back.dto.LoginRequest;
import devcourses.backvue.back.dto.SignUpRequest;
import devcourses.backvue.back.model.User;
import devcourses.backvue.back.repository.UserRepository;
import devcourses.backvue.back.security.JwtTokenProvider;

/*
Diferences between spring or not spring
In short, without Spring, you write the code that 
builds your application's object graph. With Spring, 
you declare the rules for the graph, and Spring builds 
it for you.

Withoyt spring  You are responsible for:
Instantiation: Calling new on every single object.
Wiring: Passing the created objects into the 
constructors of other objects that need them.
Lifecycle Management: Deciding when these objects 
are created and destroyed

Spring's Dependency Injection framework automates 
all of this. You simply declare the dependencies in 
the constructor (like you did in AuthService), and 
Spring's ApplicationContext handles the entire 
creation and wiring process for you based on your 
@Bean definitions and @Component scans


*/

//when the application starts @service start and 
//enter the constructor.

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    // from file JwtTokenProvider.java
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Function that return a string and receives LoginRequest Object
    // With the values sending by the front end recently
    /*
     * ok. so. this takes the information of login public String
     * authenticateUser(LoginRequest loginRequest) { then this
     * is called Authentication authentication =
     * authenticationManager.authenticate(
     * then this new UsernamePasswordAuthenticationToken(
     * loginRequest.getEmail(),
     * loginRequest.getPassword()));
     * put the values of emanul and password in a type of container
     * and then
     * 
     * in inner way the method hashes and compares and returns
     * the results that stores in authentication.
     * 
     * finally this is returned as a string. uff isnt?
     * 
     */
    public String authenticateUser(LoginRequest loginRequest) {
        // Authentication authentication
        // Represents the token for an authentication request
        // or for an authenticated principal once the
        // request has been processed by the
        // AuthenticationManager.authenticate(Authentication)
        // method.
        Authentication authentication = authenticationManager.authenticate(
                /*
                 * As Input (an Authentication Request): Before a user is
                 * verified, Authentication acts as a container
                 * for the credentials they've submitted.
                 * 
                 * 
                 * The line authenticationManager.authenticate(...) is
                 * the core of the login process in a Spring Security
                 * application.
                 * 
                 * Authentication Manager comes from here
                 * import org.springframework.security.authentication.
                 * AuthenticationManager;
                 * 
                 * AuthenticationManager authenticationManager
                 * 
                 * Because AuthService is a Spring bean (marked with
                 * 
                 * @Service), Spring's framework is responsible for
                 * creating it. When it does, it sees that the constructor
                 * requires an AuthenticationManager. Spring then looks
                 * for a bean of type AuthenticationManager that it knows
                 * about and automatically "injects" or provides it for you.
                 */

                /*
                 * The line new UsernamePasswordAuthenticationToken(...)
                 * creates a standard object used by Spring Security to
                 * hold the credentials that a user has submitted for
                 * login.
                 * 
                 * If the authenticationManager successfully validates
                 * them, it will return a new, authenticated
                 * Authentication object that contains the user's
                 * full details and their assigned roles (authorities).
                 * 
                 * 
                 */
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(), // usamos email como username
                        loginRequest.getPassword()));

        /*
         * SUMMARY OF RETURN VALUES & WHAT HAPPENS HERE: ABOVE
         * 
         * 1. authenticationManager.authenticate(...):
         * - You hand off the email & password to this method.
         * - The code inside lives in the Spring Security library (ProviderManager).
         * - Spring hashes the password, checks the database using
         * CustomUserDetailsService,
         * and verifies if it matches.
         * - If successful, Spring CONSTRUCTS AND RETURNS a fully authenticated
         * 'Authentication' object (like a digital ID Badge) containing the
         * Principal (user info) and Authorities (roles).
         * - The '=' sign assigns this returned object to the 'authentication' variable.
         * 
         * 2. tokenProvider.generateToken(authentication):
         * - We pass that "ID Badge" into our JwtTokenProvider.
         * - It reads the email and role, signs a new JWT, and RETURNS it as a String.
         * 
         * 3. The outer method (authenticateUser) then RETURNS this String back to the
         * AuthController.
         */ // si llega aquí, credenciales correctas
        return tokenProvider.generateToken(authentication);
    }

    public User registerUser(SignUpRequest signUpRequest) {
        /*
         * This line is a critical validation step in your user
         * registration process. Its purpose is to prevent
         * duplicate accounts by ensuring that every user has a
         * unique email address.
         */
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        /*
         * User user = new User();
         * Think of it as preparing a blank form that you're
         * about to fill in before saving it to the database.
         * 
         * The major part fo the code its for settings the values
         * of the user like nombre, email, password, role.
         */

        /*
         * In the setPassword these is doing the next thing
         * In summary, this line says: "Take the user's plain-text
         * password, use a strong cryptographic function to turn
         * it into a secure and irreversible hash, and set that
         * hash as the user's password to be stored in the database."
         * 
         */
        User user = new User();
        user.setNombre(signUpRequest.getNombre());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setRol(signUpRequest.getRol());

        // Use the role from the request, or default to "USER"
        String rol = signUpRequest.getRol();
        // If role is null or doesnt have value
        if (rol == null || rol.isBlank()) {
            rol = "USER";
        }
        // Se set el Role al valor del Rol que no estpa vacio
        // a letra Maúscula toUpperCase()
        // .save(user): This is the primary method for saving
        // entities. It's intelligent and performs a "save or
        // update" operatio fn.
        user.setRol(rol.toUpperCase());

        return userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User updateUser(Long id, SignUpRequest signUpRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setNombre(signUpRequest.getNombre());
        user.setEmail(signUpRequest.getEmail());

        if (signUpRequest.getPassword() != null && !signUpRequest.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        }

        if (signUpRequest.getRol() != null && !signUpRequest.getRol().isBlank()) {
            user.setRol(signUpRequest.getRol().toUpperCase());
        }

        return userRepository.save(user);
    }
}