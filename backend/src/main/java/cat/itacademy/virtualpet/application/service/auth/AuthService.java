package cat.itacademy.virtualpet.application.service.auth;

import cat.itacademy.virtualpet.application.dto.auth.AuthResponse;
import cat.itacademy.virtualpet.application.dto.auth.LoginRequest;
import cat.itacademy.virtualpet.application.dto.auth.RegisterRequest;
import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import cat.itacademy.virtualpet.infrastructure.security.JwtService;
import cat.itacademy.virtualpet.web.error.EmailAlreadyRegisteredException;
import cat.itacademy.virtualpet.web.error.UsernameAlreadyTakenException;
import cat.itacademy.virtualpet.web.error.InvalidEmailException;
import cat.itacademy.virtualpet.web.error.IncorrectPasswordException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public AuthResponse register(RegisterRequest in) {
        final String username = sanitize(in.getUsername());
        final String email = sanitizeEmail(in.getEmail());

        log.info("AUTH → REGISTER attempt username='{}' email='{}'", username, email);

        if (userRepository.existsByEmail(email)) {
            log.warn("AUTH → REGISTER email already registered: {}", email);
            throw new EmailAlreadyRegisteredException(email);
        }
        if (userRepository.existsByUsername(username)) {
            log.warn("AUTH → REGISTER username already taken: {}", username);
            throw new UsernameAlreadyTakenException(username);
        }

        final String hash = passwordEncoder.encode(in.getPassword());

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(hash)
                .roles(Set.of("ROLE_USER"))
                .build();

        userRepository.save(user);
        log.info("AUTH → REGISTER success userId={} email={}", user.getId(), email);

        String token = jwtService.generateToken(user);
        log.debug("AUTH → REGISTER JWT issued for userId={}", user.getId());

        return AuthResponse.builder()
                .token(token)
                .build();
    }


    public AuthResponse login(LoginRequest in) {
        final String email = sanitizeEmail(in.getEmail());
        log.info("AUTH → LOGIN attempt email='{}'", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("AUTH → LOGIN invalid email: {}", email);
                    return new InvalidEmailException();
                });

        if (!passwordEncoder.matches(in.getPassword(), user.getPasswordHash())) {
            log.warn("AUTH → LOGIN incorrect password for userId={} email={}", user.getId(), email);
            throw new IncorrectPasswordException();
        }

        String token = jwtService.generateToken(user);
        log.info("AUTH → LOGIN success userId={} email={}", user.getId(), email);
        log.debug("AUTH → LOGIN JWT issued for userId={}", user.getId());

        return AuthResponse.builder()
                .token(token)
                .build();
    }

    // -------- helpers --------
    private String sanitizeEmail(String raw) {
        return raw == null ? null : raw.trim().toLowerCase();
    }
    private String sanitize(String raw) {
        return raw == null ? null : raw.trim().replaceAll("\\s+", " ");
    }
}
