package devcourses.backvue.back.controller;

import java.util.List;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import devcourses.backvue.back.dto.LoginRequest;
import devcourses.backvue.back.dto.SignUpRequest;
import devcourses.backvue.back.model.User;
import devcourses.backvue.back.security.JwtAuthenticationResponse;
import devcourses.backvue.back.service.AuthService;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /*
     * The frontend then sends an HTTP POST request to your
     * backend's login endpoint (e.g., /api/signin). The body
     * of this request contains the user's credentials in
     * plain text (over a secure HTTPS connection, of course).
     * 
     * ResponseEntity: This is a powerful Spring class that
     * represents the entire HTTP response. It gives you full
     * control over the response's status code (like 200 OK
     * or 401 Unauthorized), headers, and the response body.
     * 
     * <?> (Wildcard): This means the body of the response
     * can be of any type. In your case, on success, you
     * return a JwtAuthenticationResponse object, and on
     * failure, you return a String
     * 
     * public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest
     * loginRequest) {
     * This is the name of the method
     * 
     * @RequestBody: This annotation tells Spring:
     * "Look at the body of the incoming HTTP request,
     * which should be in JSON format, and automatically
     * convert it into a Java object
     * 
     * LoginRequest loginRequest is an object created whit
     * this information
     * LoginRequest.java
     * package com.backdevc.back.dto;
     * 
     * public class LoginRequest {
     * private String email;
     * private String password;
     * 
     * public LoginRequest() {
     * }
     * 
     * public String getEmail() {
     * return email;
     * }
     * 
     * public void setEmail(String email) {
     * this.email = email;
     * }
     * 
     * public String getPassword() {
     * return password;
     * }
     * 
     * public void setPassword(String password) {
     * this.password = password;
     * }
     * }
     * 
     * this
     * {
     * "email": "user@example.com",
     * "password": "password123"
     * }
     * 
     * ...this method signature ensures that the JSON is
     * automatically turned into a LoginRequest
     * 
     * so when in the function public ResponseEntity<?>
     * authenticateUser(@RequestBody LoginRequest loginRequest)
     * { i create LoginRequest loginRequest it store in
     * LoginRequest object from LoginRequest.java
     * automatically in the private String email;
     * private String password; with all his methods
     * like setPassword. ISNT
     */

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            String jwt = authService.authenticateUser(loginRequest);
            // This calls an authService wuth authenticateUser
            // (with the object loginRequest)
            // Here in twt receives the token from the function
            // that is in authService

            // This line is responsible for constructing and
            // sending a successful HTTP response back to the
            // front-end application after a user has logged in.

            /*
             * Class that represents the entire HTTP response. It
             * gives you complete control over the response's status
             * code (like 200 OK), its headers, and its body.
             * 
             * .ok(...): This is a convenient static method of
             * ResponseEntity that creates a response with an
             * HTTP 200 OK status code. This is the standard
             * status for a successful request. The object you
             * pass inside the parentheses becomes the body of
             * the response.
             * 
             * 
             * You pass the jwt string (the token you generated in the
             * AuthService) into its constructor.
             * When this JwtAuthenticationResponse object is sent in
             * the response, Spring automatically converts it into a
             * JSON object.
             * 
             * it's not the JwtAuthenticationResponse object itself that
             * performs the conversion. Instead, the Spring Framework
             * automatically converts it for you.
             * 
             * Magic ocurres when combination of your @RestController
             * annotation and a component within Spring called an
             * HttpMessageConverter, which typically uses the Jackson
             * JSON library.
             * 
             * @RestController Annotation: When you annotate your
             * AuthController with @RestController, you are telling
             * Spring that the return value of your methods should be
             * written directly to the HTTP response body, not used to
             * look up a view or template.
             * 
             * So, the JwtAuthenticationResponse object is just a simple
             * data holder. The Spring Framework, using the powerful
             * Jackson library behind the scenes, does all the heavy
             * lifting to automatically turn your Java object into this
             * clean JSON response:
             * 
             * json
             * {
             * "accessToken": "eyJhbGciOi..."
             * }
             * 
             * 
             * 
             */
            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
            // From JwtAuthenticationResponse.java
            // Here, you create a new instance of your
            // JwtAuthenticationResponse class. This is a simple Java
            // object (a DTO) whose only purpose is to hold
            // the accessToken string.

            /*
             * The Magic of @RestController: Because your AuthController
             * is annotated with @RestController, Spring knows that the
             * return value of the method should be written directly into
             * the HTTP response body.
             * 
             * Automatic JSON Conversion
             * 
             * Jackson's Work: The Jackson converter inspects your
             * JwtAuthenticationResponse class. It finds the public
             * getter method getAccessToken(). It uses the name of this
             * method to create a JSON key ("accessToken") and calls the
             * method to get its value (the JWT string).
             * 
             * In short, JwtAuthenticationResponse acts as a structured
             * container for your token, and Spring, with the help of the
             * Jackson library, automatically handles the conversion of
             * that container into a clean JSON object for the client.
             * 
             * 
             */

        } catch (AuthenticationException e) {
            // Return a structured error message for the frontend
            /*
             * .status(HttpStatus.UNAUTHORIZED): This is a
             * builder method that sets the HTTP status code of
             * the response.
             * HttpStatus.UNAUTHORIZED corresponds to the HTTP
             * 401 Unauthorized status code.
             * 
             * In summary, this line of code says:
             * "The authentication failed. Send an HTTP 401
             * Unauthorized response back to the client, and
             * include the message 'Invalid email or password'
             * in the response body."
             * 
             */
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    /*
     * The next thing is to signup a user
     * This represent the all the entire HTTP response. status
     * 202 created, 409 conflict and <?> says i can be any type.
     * registerUser is the name of the method.
     * 
     * @RequestBody automatically convert it into a
     * SignUpRequest Java object. So, a JSON object
     * like {"nombre": "Jane Doe", "email": "jane@example.com",
     * "password": "password123"} is seamlessly mapped to the
     * fields of your signUpRequest object.
     * 
     * So. registerUser And receives from front end the
     * values nombre, email, password and role that match
     * with SignUpRequest and all his methods.
     * This converts in JSON format Automatically for @RequestBody
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpRequest signUpRequest) {
        /*
         * User result is a type container where store the user iformation
         * like email, nombre, passwordand role.
         * User is from import com.backdevc.back.model.User;
         * And send to AuthService-registerUser(signupRequest)
         * So, we will Check registerUser in AuthService
         */
        User result = authService.registerUser(signUpRequest);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(authService.findAllUsers());
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK..Runnning...");
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long id) {
        authService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @RequestBody SignUpRequest signUpRequest) {
        User updatedUser = authService.updateUser(id, signUpRequest);
        return ResponseEntity.ok(updatedUser);
    }
}