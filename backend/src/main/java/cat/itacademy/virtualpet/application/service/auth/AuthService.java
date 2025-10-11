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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Registro:
     * - normaliza email/username
     * - valida duplicados → 409 (excepciones propias)
     * - guarda BCrypt y ROLE_USER
     * - devuelve JWT en AuthResponse
     */
    public AuthResponse register(RegisterRequest in) {
        final String username = sanitize(in.getUsername());
        final String email = sanitizeEmail(in.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
        if (userRepository.existsByUsername(username)) {
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

        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .build();
    }

    /**
     * Login por email:
     * - email inexistente → InvalidEmailException (401)
     * - password incorrecta → IncorrectPasswordException (401)
     * - OK → devuelve JWT
     */
    public AuthResponse login(LoginRequest in) {
        final String email = sanitizeEmail(in.getEmail());

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidEmailException::new);

        if (!passwordEncoder.matches(in.getPassword(), user.getPasswordHash())) {
            throw new IncorrectPasswordException();
        }

        String token = jwtService.generateToken(user);
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
