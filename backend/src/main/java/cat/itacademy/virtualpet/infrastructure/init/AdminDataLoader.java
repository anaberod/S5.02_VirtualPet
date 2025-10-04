package cat.itacademy.virtualpet.infrastructure.init;

import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Creates an initial admin user if none exists.
 * This runs automatically at application startup.
 */
@Component
@RequiredArgsConstructor
public class AdminDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        final String adminEmail = "admin@example.com";
        final String adminPassword = "admin123";

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .username("admin")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .roles(Set.of("ROLE_ADMIN"))
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Admin user created: " + adminEmail + " / " + adminPassword);
        } else {
            System.out.println("ℹ️ Admin user already exists: " + adminEmail);
        }
    }
}
