package cat.itacademy.virtualpet.application.service.auth;

import cat.itacademy.virtualpet.application.dto.auth.AuthResponse;
import cat.itacademy.virtualpet.application.dto.auth.LoginRequest;
import cat.itacademy.virtualpet.application.dto.auth.RegisterRequest;
import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import cat.itacademy.virtualpet.infrastructure.security.JwtService; // <-- Lo crearemos en el Paso 4
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

/**
 * Servicio de autenticación:
 * - register: crea usuario (email único), hashea password y devuelve JWT.
 * - login: valida credenciales (email + password) y devuelve JWT.
 */
@Service
@RequiredArgsConstructor // inyección por constructor de los final
public class AuthService {

    private final UserRepository userRepository;   // Acceso a BD de usuarios
    private final PasswordEncoder passwordEncoder; // BCrypt (bean declarado en SecurityConfig)
    private final JwtService jwtService;           // Firmado/validación de JWT (Paso 4)

    /**
     * Registro de usuario:
     * - Normaliza email (trim + lower).
     * - Verifica duplicados (email/username) -> 409.
     * - Guarda hash BCrypt, asigna ROLE_USER.
     * - Devuelve token JWT.
     */
    public AuthResponse register(RegisterRequest in) {
        // Normalizar entradas
        final String username = in.getUsername().trim();
        final String email = in.getEmail().trim().toLowerCase();

        // Duplicados -> 409 Conflict
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email ya está en uso");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username ya está en uso");
        }

        // Hash de contraseña
        final String hash = passwordEncoder.encode(in.getPassword());

        // Construir entidad
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(hash)
                .roles(Set.of("ROLE_USER")) // rol por defecto
                .build();

        // Guardar en BD
        user = userRepository.save(user);

        // Emitir JWT
        String token = jwtService.generateToken(user);
        return AuthResponse.builder().token(token).build();
    }

    /**
     * Login por EMAIL (únicamente):
     * - Normaliza email.
     * - Busca por email -> si no existe, 401.
     * - Verifica password -> si no coincide, 401.
     * - Devuelve JWT.
     */
    public AuthResponse login(LoginRequest in) {
        final String email = in.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "credenciales inválidas"));

        boolean ok = passwordEncoder.matches(in.getPassword(), user.getPasswordHash());
        if (!ok) {
            // Mensaje genérico por seguridad (no revelar si el email existe)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "credenciales inválidas");
        }

        String token = jwtService.generateToken(user);
        return AuthResponse.builder().token(token).build();
    }
}
