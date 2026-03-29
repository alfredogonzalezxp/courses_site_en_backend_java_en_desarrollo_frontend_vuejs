package devcourses.backvue.back;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import devcourses.backvue.back.model.User;
import devcourses.backvue.back.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@example.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            User adminUser = new User();
            adminUser.setNombre("Admin");
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode("alf")); // Use a strong password in production
            adminUser.setRol("ADMIN"); // Assumes your Role enum has an ADMIN value
            userRepository.save(adminUser);
            logger.info("Created default admin user with email: {}", adminEmail);
        }
    }
}