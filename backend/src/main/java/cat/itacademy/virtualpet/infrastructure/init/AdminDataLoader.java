package cat.itacademy.virtualpet.infrastructure.init;

import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;


@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        final String adminEmail = "admin@example.com";
        final String adminPassword = "admin123";

        try {
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = User.builder()
                        .username("admin")
                        .email(adminEmail)
                        .passwordHash(passwordEncoder.encode(adminPassword))
                        .roles(Set.of("ROLE_ADMIN"))
                        .build();

                userRepository.save(admin);
                log.info("✅ Admin user created successfully: email='{}'", adminEmail);
            } else {
                log.info("ℹ️ Admin user already exists: email='{}'", adminEmail);
            }
        } catch (Exception e) {
            log.error("❌ Error creating initial admin user: {}", e.getMessage(), e);
        }
    }
}
